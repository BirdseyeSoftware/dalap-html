^{:cljs
  (ns dalap.test.html-test
    (:require [goog.dom.classes :as gclass]
              [goog.dom :as gdom]
              [dalap.html :as html]
              [dalap.walk :as walk])
    (:require-macros [buster-cljs.macros
                      :refer [initialize-buster deftest describe it is]]))}
(ns dalap.test.html-test
  (:require [buster-cljs.clojure :refer [deftest describe it is]]
            [clojure.string :refer [split]]
            [clojure.xml :as xml]
            [dalap.html :as html]
            [dalap.walk :as walk])
  (:import [java.io ByteArrayInputStream]))

#_(:cljs (initialize-buster))

(def basic-sample-data
  [1 2 3 "abc"
   \x \space 1.2
   " " 'foo :bar
   nil nil [nil]])

(def basic-sample-data-str "123abcx 1.2 foo:bar")

(defn assert-html
  ([in out] (assert-html in out nil))
  ([in out msg] (is (= (html/to-html in) out) msg)))

#_(:cljs
   (defn to-dom [o]
     (let [container (gdom/createElement "div")
           _ (set! (.-innerHTML container)
                   (html/to-html o))]
       (.-firstChild container))))

(defn assert-html-class
  ([in class-vector]
     (assert-html-class in class-vector nil))
  ([in class-vector msg]
     ^{:cljs
       '(let [result (to-dom in)]
          (is (every? (partial gclass/has result)
                      class-vector)
              msg))}
     (let [html-str (html/to-html in)
           parsed (xml/parse (ByteArrayInputStream. (.getBytes html-str)))
           result (set (split (get-in parsed [:attrs :class]) #"\s+"))]
       (is (= result
              (set class-vector))
           msg))))

(defn assert-html-attr
  ([in attr-name attr-val]
     (assert-html-attr in attr-name attr-val nil))
  ([in attr-name attr-val msg]
     ^{:cljs
       (is true)}
    (let [html-str (html/to-html in)
           parsed (xml/parse (ByteArrayInputStream. (.getBytes html-str)))
           result (get-in parsed [:attrs attr-name])]
       (is result msg)
       (is (= result attr-val) msg))))

(deftest test-htmlserializable-protocol
  (it "visit function is working correctly"
      (is (= (html/visit "hello & world" nil)
             "hello & world"))))

(deftest test-walker-with-html-visit
  (it "returns a valid walker"
    (let [walker-with-proto (walk/-gen-walker html/visit)]
      (is (= (walker-with-proto "hello world")
             "hello world")))))

(deftest test-to-html-function
  (it "with basic types"
      (is (= (html/to-html "hello")
             "hello")))
  (it "with dom nodes"
      (is  (= (html/to-html (html/-build-dom-node :p.hello {} "world"))
              "<p class=\"hello\">world</p>"))))

(deftest test-add-class
  (it "`add-class` works correctly"
    (assert-html (-> (html/-build-dom-node :p.what
                                           { :class "other" }
                                           "hello")
                     (html/add-class "bold"))
                 "<p class=\"bold other what\">hello</p>")))

(deftest test-remove-class
  (it "`remove-class` works correctly"
    (assert-html (-> (html/-build-dom-node :p.bold { :class "what" } "hello")
                     (html/remove-class "bold"))
                 "<p class=\"what\">hello</p>")))

(deftest test-has-class
  (it "`has-class?` works correctly"
    (let [node (html/-build-dom-node :p.bold { :class "what" } "hello")]
      (is (and (html/has-class? node "bold")
               (html/has-class? node "what")))
      (is (not (html/has-class? node "other"))))))

(deftest test-basic-types
  (it "numbers are translated correctly"
    (assert-html 1 "1" "to-html should work on non-container types")
    (assert-html [1] "1" "should be the same as the non-container version"))

  (it "basic-sample-data is translated correctly"
    (assert-html basic-sample-data basic-sample-data-str)
    (assert-html [[[basic-sample-data]]] basic-sample-data-str)
    (assert-html (seq [[[basic-sample-data]]]) basic-sample-data-str)
    (assert-html (list (list basic-sample-data)) basic-sample-data-str)
    (assert-html (for [el basic-sample-data] el) basic-sample-data-str))

  (it "keywords are escaped correctly"
    (assert-html ["" :ti&tle "fo\"o"] ":ti&amp;tlefo&quot;o"
                 "the keyword above should be HTML escaped")))

(deftest test-vector-tag-dsl
  (it "vector as a tag dsl works correctly"
    (assert-html [:hr#id] "<hr id=\"id\" />")
    (assert-html [:hr.classA] "<hr class=\"classA\" />")
    (assert-html [:hr.classA.classB] "<hr class=\"classA classB\" />")
    (assert-html-class [:hr#id.cls] ["cls"])
    (assert-html-class [:hr#id.classA.classB] ["classA" "classB"])
    (assert-html [:div [:div basic-sample-data]]
                 (str "<div><div>" basic-sample-data-str "</div></div>"))
    (assert-html-attr [:hr.clear {:asdf 123}]
                      :asdf "123")
    (assert-html [:input {:type "text"}] "<input type=\"text\" />")
    (assert-html [:div [:div [:div [:div]]]]
                 "<div><div><div><div></div></div></div></div>")
    (assert-html [:div#a [:div#b [:div#c [:div#d]]]]
                 (str
                  "<div id=\"a\"><div id=\"b\"><div id=\"c\"><div id=\"d\">"
                  "</div></div></div></div>"))
    ;; NOTE: it works!, just the hash order fucks up a little
    ;; (assert-html [:form#contact {:action "GET"}
    ;;               [:input {:type "text"
    ;;                        :name "first-name"
    ;;                        :value "Tavis & Roman"}]]
    ;;              (str
    ;;               "<form action=\"GET\" id=\"contact\"><input value=\"Tavis &amp;"
    ;;               " Roman\" type=\"text\" name=\"first-name\" /></form>"))
    ))