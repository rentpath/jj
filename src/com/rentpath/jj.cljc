(ns com.rentpath.jj
  (:require [clojure.string :as str]
            #?(:clj [cheshire.core :refer [generate-string]])
            [com.rentpath.jj.parser :refer [parse]]
            [com.rentpath.jj.util :refer [deep-merge]]))

;; TODO Consider use of transit-cljs
#?(:cljs (defn generate-string [obj options] (.stringify js/JSON (clj->js obj) nil 2)))

(defn eval-jj
  [forms]
  (cond
    (map? forms) forms
    (= 1 (count forms)) (first forms)
    (every? map? forms) (apply deep-merge forms)
    :else forms))

(def jj (comp eval-jj parse))

(defn json
  ([jj-str] (json jj-str {:pretty true}))
  ([jj-str options]
   (generate-string (jj jj-str) options)))

(defn println-json
  [jj-str]
  (println (json jj-str {:pretty true})))

(def ^:extern pj println-json)

(defn ppj [program]
  (println "PROGRAM")
  (println "=======")
  (println (str/replace program #"(?m)^\s+" ""))
  (println)
  (println "JSON")
  (println "====")
  (println-json program))
