^{:cljs
  (ns dalap.html
    (:require [clojure.set :refer [union]]
              [clojure.string :refer [split]]
              [dalap.walk :as walk]
              [dalap.html.escape :refer [PreEscaped safe escape-html]]
              [dalap.defaults :as defaults])
    (:require-macros [clojure.core.match.js :refer [match]]))}
(ns dalap.html
  (:require [clojure.set :refer [union]]
            [clojure.string :refer [split]]
            [clojure.core.match :refer [match]]
            [dalap.walk :as walk]
            [dalap.html.escape :refer [safe escape-html]]
            [dalap.defaults :as defaults])

  (:import [clojure.lang IPersistentVector]
           [dalap.html.escape PreEscaped]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; General purpose utility functions
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-set [x]
  (cond
    (nil? x) (sorted-set)
    (sequential? x) (apply sorted-set x)
    :else (sorted-set x)))

(defn nil-or-empty? [x]
  (or (nil? x)
      (empty? x)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Tag parsing utility functions
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^{:doc "Regexp for CSS-style id or class in a tag name."}
  re-tag  #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(def ^:private maybe-empty-tags
  (set (map name
    '(area base br col frame hr img input link meta p param))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Protocols & Types for HTML serialization
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord TagAttrs [tag attrs-map])
(defrecord DomNode [tag attrs content])

(defn visit-tag-attrs [attrs w]
  (let [{:keys [tag attrs-map]} attrs]
    (w (for [[k v] attrs-map]
         (cond
          (= :class k)
          [" " (name k) (safe \= \")
           (interpose " " v)
           (safe \")]
          :else
          [" " (name k) (safe \= \") v (safe \")])))))

(defn visit-dom-node  [dom-node w]
  (let [{:keys [tag attrs content]} dom-node]
    (let [tag-name (name tag)
        is-empty (and (maybe-empty-tags tag-name) (empty? content))
        open-tag [(safe \<) tag-name attrs
                  (if is-empty (safe " />") (safe \>))]
        close-tag [(safe "</") tag-name (safe \>)]]
    (w [open-tag (if-not is-empty [content close-tag])]))))


(defprotocol HtmlSerializable
  (visit [x w]))

(extend-protocol HtmlSerializable
  Object (visit [n w] (defaults/visit n w))
  nil (visit [_ _] "")
  ^{:cljs number} Number (visit [n w] (safe (str n)))
  PreEscaped (visit [x _] x)
  DomNode (visit [dn w] (visit-dom-node dn w))
  TagAttrs (visit [ta w] (visit-tag-attrs ta w)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; DomNode builder helpers

(defn merge-tag-attrs [tag-attrs id clazz]
  (let [
    base-attrs (->> {:id id :class clazz}
                    (filter #(not (nil-or-empty? (nth % 1))))
                    (into {}))
    attr-merge (fn [result [k v]]
                 (cond
                   (= k :class) (assoc result
                                       k
                                       (union (:class result #{})
                                              (make-set v)))
                   :else (assoc result k v)))]
  (reduce attr-merge base-attrs tag-attrs)))

(defn norm-dom-node-classes [classes]
  (make-set
   (if (string? classes)
     (split classes #"\.")
     classes)))

(defn -build-dom-node
  ([tag] (-build-dom-node tag {} []))
  ([tag attrs] (-build-dom-node tag attrs []))
  ([tag attrs content]
     (if-let [tag-match (re-matches re-tag (name tag))]
       (let [[_ tag id classes] tag-match
             tag-name (name tag)
             tag-attrs (TagAttrs.
                        tag-name
                        (merge-tag-attrs attrs id (norm-dom-node-classes classes)))]
         (DomNode. tag-name tag-attrs content))
       (throw (new
               ^{:cljs js/Error} Exception
               (format (str "This is an invalid dom node tag: %s."
                            " Should be in the form :tagname#id.class")
                       (name tag)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Utility functions for adding/removing HTML classes on
;; DomNode types

(defn dom-node? [node]
  (instance? DomNode node))

(defn alter-class [node f]
  (update-in node [:attrs :attrs-map :class] f))

(defn add-class [node clazz]
  (alter-class node #(union % (make-set clazz))))

(defn remove-class [node clazz]
  (alter-class node #(disj % clazz)))

(defn has-class? [node clazz]
  ((get-in node [:attrs :attrs-map :class] (sorted-set)) clazz))

(defn has-id? [node id]
  (= (get-in node [:attrs :attrs-map :id]) id))

(defn has-tag-name? [node tag-name]
  (= (name (:tag node)) (name tag-name)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; HTML serialization for vectors


(defn visit-vector [v w]
  (let [named? (fn [x]
                 (or (symbol? x)
                     (keyword? x)))]
    ;@@TR: replace this
    (match [v]
      [([(tag :guard named?) (attrs :guard map?) & content] :seq)]
      (w (-build-dom-node tag attrs content))

      [([(tag :guard named?) & content] :seq)]
      (w (-build-dom-node tag {} content))

      [_] (defaults/visit-seq v w))))

^{:cljs
  (extend-protocol HtmlSerializable
    PersistentVector
    (visit [vec w]
      (visit-vector vec w)))}
(extend IPersistentVector HtmlSerializable {:visit visit-vector})

(defn to-html
  ([x] (to-html x visit))
  ([x visitor-fn]
     (apply escape-html (flatten (walk/walk [x] visitor-fn)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; HTML Builder helpers
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def doctype
  {:html4
   (safe "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" "
        "\"http://www.w3.org/TR/html4/safeict.dtd\">\n")
   :xhtml-safeict
   (safe "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Safeict//EN\" "
        "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-safeict.dtd\">\n")
   :xhtml-transitional
   (safe "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
        "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n")
   :html5
   (safe "<!DOCTYPE html>\n")})

(defn html5 [& contents]
  [(doctype :html5)
   [:html
    contents]])
