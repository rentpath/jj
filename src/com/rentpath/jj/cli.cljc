(ns com.rentpath.jj.cli
  (:require [clojure.string :as str]
            [com.rentpath.jj :as jj]
            #?(:cljs [cljs.nodejs :as node])
            [com.rentpath.jj.util :refer [handle-args]])
  #?(:clj (:gen-class)))

#?(:cljs (def process (js/require "process")))
#?(:cljs (node/enable-util-print!))
#?(:cljs (.on process "uncaughtException" #(js/console.error %)))

(defn -main [& args]
  (let [args (handle-args args)]
    (jj/pj (str/join " " args))))

#?(:cljs (set! *main-cli-fn* -main))
