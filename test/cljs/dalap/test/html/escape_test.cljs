;; This file was generated with lein-dalap from
;;
;; test/clj/dalap/test/html/escape_test.clj @ Sat Oct 05 15:11:54 PDT 2013
;;
(ns dalap.test.html.escape-test (:require [clojure.string :refer [upper-case]] [dalap.html.escape :as esc]) (:require-macros [buster-cljs.macros :refer [initialize-buster deftest describe it is]]))
(initialize-buster)
(deftest test-gen-str-escaper (it "creates a `str` function successfully" (is (= ((esc/-gen-str-escaper (fn* [p1__1293#] (.toString p1__1293#))) "abc" 123) "abc123") "should behave the same as `clojure.core/str`") (is (= ((esc/-gen-str-escaper (fn* [p1__1294#] (upper-case (.toString p1__1294#)))) "abc" 123) "ABC123") "should be uppercase version of `clojure.core/str`")))
(deftest test-low-level-char-escaping (it "check low level char escaping" (doseq [[k v] esc/html-escaping-map] [(is (= (esc/-escape-html-chars k) v) "test lower-level escaper") (is (= (esc/escape-html (str k)) v) "test via HtmlEscapable protocol")])))
(deftest test-HtmlEscapable (it "check HtmlEscapable protocol" (doseq [[inp outp] {nil "", (esc/safe "&") "&", (esc/safe \&) "&", (esc/safe "&-<->-\"") "&-<->-\"", \& "&amp;", "abcd&e" "abcd&amp;e", "&-<->-\"" "&amp;-&lt;-&gt;-&quot;"}] (is (= (esc/-to-html-escaped-str inp) (esc/escape-html inp)) "`-to-html-escaped-str` should behave the same way as `escape-html`"))))
(deftest test-mixed-pre-escaped-and-not (it "check pre-escaped content with non-escaped content" (is (= (esc/escape-html (esc/safe "&-<->-\"") "-&->") "&-<->-\"-&amp;-&gt;") "mixed pre-escaped and non-escaped should work interchangeably")))