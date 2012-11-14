(defproject dalap-html "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [dalap "0.0.1-SNAPSHOT"]
                 [org.clojure/core.match "0.2.0-alpha11"]

                 [org.clojure/google-closure-library "0.0-1376-2"]
                 [buster-cljs "0.1.0-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "0.2.9"]
            [lein-dalap-cljsbuild "0.1.0-SNAPSHOT"]]

  :hooks [leiningen.dalap-cljsbuild]

  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["test/clj" "test/cljs"]

  :cljsbuild
  {:builds
   [{:id :dev
     :source-path "src/cljs"
     :dalap
     {:paths
      {
       "src/clj/dalap/defaults.clj" "src/cljs/dalap/defaults.cljs"
       "src/clj/dalap/html/escape.clj" "src/cljs/dalap/html/escape.cljs"
       "src/clj/dalap/html.clj" "src/cljs/dalap/html.cljs"
       }}
     :compiler
     {:output-to "resources/js/dalap_html_dev.js"
      :optimizations :whitespace
      :pretty-print true}}
    ;;;
    {:id :browser-test
     :source-path "test/cljs"
     :dalap
     {:paths
      {"test/clj/dalap/html/test/escape_test.clj" "test/cljs/dalap/html/test/escape_test.cljs"
       "test/clj/dalap/test/html_test.clj" "test/cljs/dalap/test/html_test.cljs"
       }}
     :compiler
     {:externs ["externs/buster.js"]
      :libraries ["resources/js/dalap_html_dev.js"]
      :output-to "resources/js/dalap_html_browser_test.js"
      :optimizations :simple
      :pretty-print true}}
    ;;;
    {:id :node-test
     :source-path "test/cljs"
     :compiler
     {:externs ["externs/buster.js"]
      :libraries ["resources/js/dalap_html_dev.js"]
      :output-to "resources/js/dalap_html_node_test.js"
      :optimizations :simple
      :pretty-print true}}
    ;;;
    {:id :browser-test-optimized
     :source-path "test/cljs"
     :compiler
     {:externs ["externs/buster.js"]
      :libraries ["resources/js/dalap_html_dev.js"]
      :output-to "resources/js/dalap_html_browser_test_optimized.js"
      :optimizations :advanced}}
    ]}
  )
