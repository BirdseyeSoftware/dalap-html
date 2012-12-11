;; This file was generated with lein-dalap from
;;
;; src/clj/dalap/defaults.clj @ Tue Dec 11 20:54:01 UTC 2012
;;
(ns dalap.defaults (:use [dalap.walk :only [walk]]))
(defprotocol Serializable "A protocol providing the default implementation of Dalap's\n  serialization visitor interface." (visit [x walker]))
(extend-protocol Serializable nil (visit [_ _] "") string (visit [s _] s) number (visit [n _] (str n)))
(defn visit-seq [s w] (map w s))
(extend-protocol Serializable default (visit [obj w] (cond (satisfies? ISeq obj) (visit-seq obj w) :else (throw (missing-protocol Serializable obj)))))
(defn serialize "Walks, flattens and serializes an object (or graph of objects) into\n  a String.  The default implementation does no escaping of the output." ([x] (serialize x visit str)) ([x visitor] (serialize x visitor str)) ([x visitor output-escaper] (apply output-escaper (flatten (walk [x] visitor)))))