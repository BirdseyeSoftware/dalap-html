^{:cljs
  (ns dalap.html.escape
    (:require [clojure.string :as str]
              [goog.string :as gstr]
              [goog.string.StringBuffer :as gstringbuffer]))}
(ns dalap.html.escape
  (:require [clojure.string :as str]))

(deftype PreEscaped [val]
  ^{:cljs Object}
  Object
  (toString [_] (str val)))

(defn safe
  "Transforms one or several objects into a String and then
  wraps it into a `dalap.escape/PreEscaped` record. When using
  escaping functions on this type, they will only unwrap the value without
  doing any escaping whatsoever to the inner String."
  [& xs] (PreEscaped. (apply str xs)))

(defn -gen-str-escaper
  "Transforms a function that escapes a single string into one that
  accepts multiple args or a seq of strings.

  Example:

  (-gen-str-escaper #(. %1 toString))

  returns the equivalent of `clojure.core/str`"
  [single-string-fn]
  (fn ^String str**
    (^String [] "")
    (^String [^Object x] (single-string-fn x))
    (^String [x & ys]
             (let [step (fn gen-str-step [^StringBuilder sb more]
                          (if more
                            (recur (.append sb (str** (first more))) (next more))
                            (.toString sb)))
                   builder
                   ^{:cljs (gstr/StringBuffer. (str** x))}
                   (StringBuilder. (str** x))]
               (step builder ys)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def
  ^{:doc "Map of special HTML characters to it's _escaped_ string
          representation"}
  html-escaping-map {
    \& "&amp;"
    \< "&lt;"
    \> "&gt;"
    \" "&quot;"
  })

(defn -escape-html-chars
  "Escapes string's characters that are contained in the
  `dalap.escape.html/html-escaping-map`, in case an object
  is given, the `clojure.core/str` will be applied to it."
  [s]
  (str/escape (str s) html-escaping-map))

;;;;;;;;;;;;;;;;;;;;

;; IMPORTANT:
;; we avoid having a default implementation on Object
;; to not have subtle/silent bugs
(defprotocol HtmlEscapable
  (-to-html-escaped-str
    [x]
    "Transforms a record into a String. The resulting string
    _must be_ HTML escaped. In order to implement this functions for your
    objects we recommed using the `dalap.escape.html/-escape-html-chars`
    function.

    This function _should not_ be called directly, use
    `dalap.escape.html/escape-html` function instead."))

;;;;;;;;;;;;;;;;;;;;

(extend-protocol HtmlEscapable
  PreEscaped
  (-to-html-escaped-str [x] (str x)))

;;;;;;;;;;;;;;;;;;;;

(extend-protocol HtmlEscapable
  nil
  (-to-html-escaped-str [_] ""))

;;;;;;;;;;;;;;;;;;;;

(extend-protocol HtmlEscapable
  java.lang.String
  (-to-html-escaped-str [s]
    (-escape-html-chars s)))

;;;;;;;;;;;;;;;;;;;;

^:clj
(extend-protocol HtmlEscapable
  Character
  (-to-html-escaped-str [c]
    (-escape-html-chars c)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def
  ^{:doc "Transforms a record that implements the HtmlEscapable protocol
         into a HTML escaped String"}
  escape-html (-gen-str-escaper -to-html-escaped-str))
