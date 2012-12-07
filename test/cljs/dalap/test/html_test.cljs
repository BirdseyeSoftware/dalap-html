;; This file was generated with lein-dalap from
;;
;; test/clj/dalap/test/html_test.clj @ Fri Dec 07 20:25:11 UTC 2012
;;
(ns dalap.test.html-test (:require [goog.dom.classes :as gclass] [goog.dom :as gdom] [dalap.html :as html] [dalap.walk :as walk]) (:require-macros [buster-cljs.macros :refer [initialize-buster deftest describe it is]]))
(do (initialize-buster))
(def basic-sample-data [1 2 3 "abc" \x \space 1.2 " " (quote foo) :bar nil nil [nil]])
(def basic-sample-data-str "123abcx 1.2 foo:bar")
(defn assert-html ([in out] (assert-html in out nil)) ([in out msg] (is (= (html/to-html in) out) msg)))
(do (defn to-dom [o] (let [container (gdom/createElement "div") _ (set! (.-innerHTML container) (html/to-html o))] (.-firstChild container))))
(defn assert-html-class ([in class-vector] (assert-html-class in class-vector nil)) ([in class-vector msg] (let [result (to-dom in)] (is (every? (partial gclass/has result) class-vector) msg))))
(defn assert-html-attr ([in attr-name attr-val] (assert-html-attr in attr-name attr-val nil)) ([in attr-name attr-val msg] (is true)))
(deftest test-htmlserializable-protocol (it "visit function is working correctly" (is (= (html/visit "hello & world" nil) "hello & world"))))
(deftest test-walker-with-html-visit (it "returns a valid walker" (let [walker-with-proto (walk/-gen-walker html/visit)] (is (= (walker-with-proto "hello world") "hello world")))))
(deftest test-to-html-function (it "with basic types" (is (= (html/to-html "hello") "hello"))) (it "with dom nodes" (is (= (html/to-html (html/-build-dom-node :p.hello {} "world")) "<p class=\"hello\">world</p>"))))
(deftest test-add-class (it "`add-class` works correctly" (assert-html (-> (html/-build-dom-node :p.what {:class "other"} "hello") (html/add-class "bold")) "<p class=\"bold other what\">hello</p>")))
(deftest test-remove-class (it "`remove-class` works correctly" (assert-html (-> (html/-build-dom-node :p.bold {:class "what"} "hello") (html/remove-class "bold")) "<p class=\"what\">hello</p>")))
(deftest test-has-class (it "`has-class?` works correctly" (let [node (html/-build-dom-node :p.bold {:class "what"} "hello")] (is (and (html/has-class? node "bold") (html/has-class? node "what"))) (is (not (html/has-class? node "other"))))))
(deftest test-basic-types (it "numbers are translated correctly" (assert-html 1 "1" "to-html should work on non-container types") (assert-html [1] "1" "should be the same as the non-container version")) (it "basic-sample-data is translated correctly" (assert-html basic-sample-data basic-sample-data-str) (assert-html [[[basic-sample-data]]] basic-sample-data-str) (assert-html (seq [[[basic-sample-data]]]) basic-sample-data-str) (assert-html (list (list basic-sample-data)) basic-sample-data-str) (assert-html (for [el basic-sample-data] el) basic-sample-data-str)) (it "keywords are escaped correctly" (assert-html ["" :ti&tle "fo\"o"] ":ti&amp;tlefo&quot;o" "the keyword above should be HTML escaped")))
(deftest test-vector-tag-dsl (it "vector as a tag dsl works correctly" (assert-html [:hr#id] "<hr id=\"id\" />") (assert-html [:hr.classA] "<hr class=\"classA\" />") (assert-html [:hr.classA.classB] "<hr class=\"classA classB\" />") (assert-html-class [:hr#id.cls] ["cls"]) (assert-html-class [:hr#id.classA.classB] ["classA" "classB"]) (assert-html [:div [:div basic-sample-data]] (str "<div><div>" basic-sample-data-str "</div></div>")) (assert-html-attr [:hr.clear {:asdf 123}] :asdf "123") (assert-html [:input {:type "text"}] "<input type=\"text\" />") (assert-html [:div [:div [:div [:div]]]] "<div><div><div><div></div></div></div></div>") (assert-html [:div#a [:div#b [:div#c [:div#d]]]] (str "<div id=\"a\"><div id=\"b\"><div id=\"c\"><div id=\"d\">" "</div></div></div></div>"))))