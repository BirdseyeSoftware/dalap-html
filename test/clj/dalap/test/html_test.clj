^{:cljs
  (ns dalap.test.html-test
    (:require [goog.dom.classes :as gclass]
              [goog.dom :as gdom]
              [dalap.html :as html :refer [safe html-comment ie-cond-comment]]
              [dalap.walk :as walk])
    (:require-macros [buster-cljs.macros
                      :refer [initialize-buster deftest describe it is are]]))}
(ns dalap.test.html-test
  (:require [buster-cljs.clojure :refer [deftest describe it is are]]
            [clojure.string :refer [split]]
            [clojure.xml :as xml]
            [dalap.html :as html :refer [safe html-comment ie-cond-comment]]
            [dalap.walk :as walk])
  (:import [java.io ByteArrayInputStream]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_(:cljs (initialize-buster))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def basic-sample-data
  [1 2 3 "abc"
   \x \space 1.2
   " " 'foo :bar
   nil nil [nil]])

(def basic-sample-data-str "123abcx 1.2 foo:bar")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn assert-html
  ([in out] (assert-html in out nil))
  ([in out msg] (is (= (html/to-html in) out) msg)))

;; #_(:cljs
;;    (defn to-dom [o]
;;      (let [container (gdom/createElement "div")
;;            _ (set! (.-innerHTML container)
;;                    (html/to-html o))]
;;        (.-firstChild container))))

;; (defn assert-html-class
;;   ([in class-vector]
;;      (assert-html-class in class-vector nil))
;;   ([in class-vector msg]
;;      ^{:cljs
;;        '(let [result (to-dom in)]
;;           (is (every? (partial gclass/has result)
;;                       class-vector)
;;               msg))}
;;      (let [html-str (html/to-html in)
;;            parsed (xml/parse (ByteArrayInputStream. (.getBytes html-str)))
;;            result (set (split (get-in parsed [:attrs :class]) #"\s+"))]
;;        (is (= result
;;               (set class-vector))
;;            msg))))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest test-to-html
  (it "with primitive types"
    (are [in out] (= (html/to-html in) out)
         "hello"    "hello"
         123        "123"
         :keyword    "<keyword></keyword>"
         'symbol     "<symbol></symbol>"))

  (it "with DomNode types"
    (are [in out] (= (html/to-html (apply html/-build-dom-node in))
                     out)
         [:p.hello {} "world"]   "<p class=\"hello\">world</p>"))

  (it "with tag dsl"
    ;; (assert-html-class [:hr#id.cls] ["cls"])
    ;; (assert-html-class [:hr#id.classA.classB] ["classA" "classB"])
    (assert-html-attr [:hr.clear {:asdf 123}]
                      :asdf "123")
    (are
     [in out] (= (html/to-html in) out)

     [:hr#id]
     "<hr id=\"id\" />"

     [:hr.classA]
     "<hr class=\"classA\" />"

     [:hr.classA.classB]
     "<hr class=\"classA classB\" />"

     [:div [:div basic-sample-data]]
     (str "<div><div>" basic-sample-data-str "</div></div>")

     [:input {:type "text"}]
     "<input type=\"text\" />"

     [:div [:div [:div [:div]]]]
     "<div><div><div><div></div></div></div></div>"

     [:div#a [:div#b [:div#c [:div#d]]]]
     (str
      "<div id=\"a\"><div id=\"b\"><div id=\"c\"><div id=\"d\">"
      "</div></div></div></div>")

     [:form#contact {:action "GET"}
      [:input {:type "text"
               :name "first-name"
               :value "John & Paul"}]]
     (str
      "<form action=\"GET\" id=\"contact\">"
      "<input name=\"first-name\" type=\"text\" value=\"John &amp; Paul\" />"
      "</form>"))))

(deftest test-html-comments
  (it "html & IE conditional comments are handled"
    (are
     [in out] (= (html/to-html in) out)

     (html-comment "testing")
     "<!-- testing -->"

     (html-comment "escaped>&")
     "<!-- escaped&gt;&amp; -->"

     (html-comment (safe "not escaped>&"))
     "<!-- not escaped>& -->"

     (html-comment [:div "test"])
     "<!-- <div>test</div> -->"

     (ie-cond-comment "IE 7" "test")
     "<!--[if IE 7]test<![endif]-->"
     (ie-cond-comment "lt IE 7" "test")
     "<!--[if lt IE 7]test<![endif]-->"
     (ie-cond-comment "IE 7" [:div "foo"])
     "<!--[if IE 7]<div>foo</div><![endif]-->")))


(deftest test-htmlserializable-protocol
  (it "visit function is working correctly"
      (is (= (html/visit "hello & world" nil)
             "hello & world"))))

(deftest test-dom-node-utils
  (it "`add-class` works correctly"
    (assert-html (-> (html/-build-dom-node :p.what
                                           { :class "other" }
                                           "hello")
                     (html/add-class "bold"))
                 "<p class=\"bold other what\">hello</p>"))

  (it "`remove-class` works correctly"
    (assert-html (-> (html/-build-dom-node :p.bold { :class "what" } "hello")
                     (html/remove-class "bold"))
                 "<p class=\"what\">hello</p>"))

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
    (are
     [in out]
     (= (html/to-html in) out)

     basic-sample-data basic-sample-data-str
     [[[basic-sample-data]]] basic-sample-data-str
     (seq [[[basic-sample-data]]]) basic-sample-data-str
     (list (list basic-sample-data)) basic-sample-data-str
     (for [el basic-sample-data] el) basic-sample-data-str))

  (it "keywords are escaped correctly"
    (assert-html ["" :ti&tle "fo\"o"] ":ti&amp;tlefo&quot;o"
                 "the keyword above should be HTML escaped")))
