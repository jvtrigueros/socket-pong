(defproject socket-pong "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.5.3"
  :source-paths ["src/clj" "script"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]
                 [quil "2.3.0"]
                 ;; ClojureScript
                 [org.clojure/clojurescript "1.7.170"]
                 [figwheel-sidecar "0.5.0"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-figwheel "0.5.0-1"]
            [lein-cljsbuild "1.1.1"]]
  :hooks [leiningen.cljsbuild]
  :ring {:handler socket-pong.handler/app}
  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src/cljs" "test/cljs"]
                :figwheel     true
                :compiler     {:main                 socket-pong.app
                               :asset-path           "js/compiled/out"
                               :output-to            "resources/public/js/compiled/socket_pong.js"
                               :output-dir           "resources/public/js/compiled/out"
                               :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id           "min"
                :source-paths ["src/cljs"]
                :compiler     {:output-to     "resources/public/js/compiled/socket_pong.min.js"
                               :main          socket-pong.app
                               :optimizations :advanced
                               :pretty-print  false}}]}

  :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring/ring-mock "0.3.0"]]}
             :uberjar {:aot :all}})
