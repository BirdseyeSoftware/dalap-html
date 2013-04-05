;; This file was generated with lein-dalap from
;;
;; src/clj/dalap/html/escape.clj @ Thu Apr 04 16:30:20 GMT-08:00 2013
;;
(ns dalap.html.escape (:require [clojure.string :as str] [goog.string :as gstr] [goog.string.StringBuffer :as gstringbuffer]))
(deftype PreEscaped [val] Object (toString [_] (str val)))
(defn safe "Transforms one or several objects into a String and then\n  wraps it into a `dalap.escape/PreEscaped` record. When using\n  escaping functions on this type, they will only unwrap the value without\n  doing any escaping whatsoever to the inner String." [& xs] (PreEscaped. (apply str xs)))
(defn -gen-str-escaper "Transforms a function that escapes a single string into one that\n  accepts multiple args or a seq of strings.\n\n  Example:\n\n  (-gen-str-escaper #(. %1 toString))\n\n  returns the equivalent of `clojure.core/str`" [single-string-fn] (fn str** ([] "") ([x] (single-string-fn x)) ([x & ys] (let [step (fn gen-str-step [sb more] (if more (recur (.append sb (str** (first more))) (next more)) (.toString sb))) builder (gstr/StringBuffer. (str** x))] (step builder ys)))))
(def html-escaping-map {\& "&amp;", \< "&lt;", \> "&gt;", \" "&quot;"})
(defn -escape-html-chars "Escapes string's characters that are contained in the\n  `dalap.escape.html/html-escaping-map`, in case an object\n  is given, the `clojure.core/str` will be applied to it." [s] (str/escape (str s) html-escaping-map))
(defprotocol HtmlEscapable (-to-html-escaped-str [x] "Transforms a record into a String. The resulting string\n    _must be_ HTML escaped. In order to implement this functions for your\n    objects we recommed using the `dalap.escape.html/-escape-html-chars`\n    function.\n\n    This function _should not_ be called directly, use\n    `dalap.escape.html/escape-html` function instead."))
(extend-protocol HtmlEscapable PreEscaped (-to-html-escaped-str [x] (str x)))
(extend-protocol HtmlEscapable nil (-to-html-escaped-str [_] ""))
(extend-protocol HtmlEscapable string (-to-html-escaped-str [s] (-escape-html-chars s)))
(def escape-html (-gen-str-escaper -to-html-escaped-str))