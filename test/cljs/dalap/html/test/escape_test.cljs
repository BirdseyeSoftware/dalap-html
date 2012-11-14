;; This file was generated with dalap-cljsbuild from
;;
;; test/clj/dalap/html/test/escape_test.clj @ Wed Nov 14 22:40:08 UTC 2012
;;
(ns dalap.html.test.escape-test (:require [clojure.string :refer [upper-case]] [buster-cljs.core :refer [is is-equal]] [dalap.html.escape :as esc]) (:require-macros [buster-cljs.macros :refer [initialize-buster deftest describe it]]))
(do (initialize-buster))
(deftest test-gen-str-escaper (it "creates a `str` function successfully" (is-equal ((esc/-gen-str-escaper (fn* [p1__3714#] (.toString p1__3714#))) "abc" 123) "abc123" "should behave the same as `clojure.core/str`") (is-equal ((esc/-gen-str-escaper (fn* [p1__3715#] (upper-case (.toString p1__3715#)))) "abc" 123) "ABC123" "should be uppercase version of `clojure.core/str`")))
(deftest test-low-level-char-escaping (it "check low level char escaping" (doseq [[k v] esc/html-escaping-map] [(is-equal (esc/-escape-html-chars k) v "test lower-level escaper") (is-equal (esc/escape-html (str k)) v "test via HtmlEscapable protocol")])))
(deftest test-HtmlEscapable (it "check HtmlEscapable protocol" (doseq [[inp outp] {nil "", (esc/safe "&") "&", (esc/safe \&) "&", (esc/safe "&-<->-\"") "&-<->-\"", \& "&amp;", "abcd&e" "abcd&amp;e", "&-<->-\"" "&amp;-&lt;-&gt;-&quot;"}] (is-equal (esc/-to-html-escaped-str inp) (esc/escape-html inp) "`-to-html-escaped-str` should behave the same way as `escape-html`"))))
(deftest test-mixed-pre-escaped-and-not (it "check pre-escaped content with non-escaped content" (is-equal (esc/escape-html (esc/safe "&-<->-\"") "-&->") "&-<->-\"-&amp;-&gt;" "mixed pre-escaped and non-escaped should work interchangeably")))