(ns dalap.defaults
  (:use [dalap.walk :only [walk]])
  ^:clj (:import [clojure.lang Named Seqable]))

(defprotocol Serializable
  "A protocol providing the default implementation of Dalap's
  serialization visitor interface."
  (visit [x walker]))

(extend-protocol Serializable
  ;; Object impl not provided to avoid subtle/silent bugs
  nil (visit [_ _] "")
  java.lang.String (visit [s _] s)
  ^:clj Character ^:clj (visit [c _] (str c))
  ^{:cljs number} Number (visit [n _] (str n))
  ^:clj Named ^:clj (visit [o _] (str o)))

(defn visit-seq [s w] (map w s))
^{:cljs
  (extend-protocol Serializable
    default
    (visit [obj w]
      (cond
        (satisfies? ISeq obj) (visit-seq obj w)
        :else (throw (missing-protocol Serializable obj)))))}
(extend Seqable Serializable {:visit visit-seq})

(defn serialize
  "Walks, flattens and serializes an object (or graph of objects) into
  a String.  The default implementation does no escaping of the output."

  ([x] (serialize x visit str))
  ([x visitor] (serialize x visitor str))

  ([x visitor output-escaper]
     (apply output-escaper (flatten (walk [x] visitor)))))
