(def rebel-readline '[com.bhauman/rebel-readline "0.1.1"])
(def cheshire       '[cheshire "5.8.0"])
(def tools-cli      '[org.clojure/tools.cli "0.3.5"])

(defproject com.rentpath/jj "0.1.0"
  :description "JSON production"
  :url "https://github.com/rentpath/jj"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 #_[org.clojure/spec.alpha "0.1.143"]
                 [instaparse "1.4.8"]
                 #_[cheshire "5.8.0"]]
  :main ^:skip-aot com.rentpath.jj
  :target-path "target/%s"
  :clean-targets [:target-path :compile-path "out"]
  :profiles {:dev {:dependencies [~rebel-readline
                                  ~cheshire
                                  ~tools-cli]}
             :cljs {:dependencies [[cljsjs/nodejs-externs "1.0.4-1"]]}
             :uberjar-repl {:dependencies [~rebel-readline
                                           ~cheshire]
                            :aot [com.rentpath.jj.repl]
                            :main com.rentpath.jj.repl
                            :uberjar-name "jj-repl.jar"}
             :uberjar-cli  {:dependencies [~cheshire
                                           ~tools-cli]
                            :aot [com.rentpath.jj.cli]
                            :main com.rentpath.jj.cli
                            :uberjar-name "jj-cli.jar"}})
