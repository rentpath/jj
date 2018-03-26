(def cheshire       '[cheshire "5.8.0"])
(def jackson-jq     '[net.thisptr/jackson-jq "0.0.8"])
(def rebel-readline '[com.bhauman/rebel-readline "0.1.1"])
(def tools-cli      '[org.clojure/tools.cli "0.3.5"])

(defproject com.rentpath/jj "0.1.0"
  :description "JSON production"
  :url "https://github.com/rentpath/jj"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.memoize "0.7.1"]
                 #_[org.clojure/spec.alpha "0.1.143"]
                 [instaparse "1.4.8"]
                 #_[cheshire "5.8.0"]]
  :main ^:skip-aot com.rentpath.jj
  :target-path "target/%s"
  :clean-targets [:target-path :compile-path "out"]
  :profiles {:dev {:dependencies [~cheshire
                                  ~jackson-jq
                                  ~rebel-readline
                                  ~tools-cli]}
             :cljs {:dependencies [[cljsjs/nodejs-externs "1.0.4-1"]]}
             :uberjar-repl {:dependencies [~cheshire
                                           ~jackson-jq
                                           ~rebel-readline]
                            :aot [com.rentpath.jj.repl]
                            :main com.rentpath.jj.repl
                            :uberjar-name "jj-repl.jar"}})
