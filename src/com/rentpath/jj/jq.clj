(ns com.rentpath.jj.jq
  "jackson-jq integration"
  (:require [clojure.core.memoize :refer [fifo]]
            [cheshire.core :as json])
  (:import [com.fasterxml.jackson.databind ObjectMapper]
           [net.thisptr.jackson.jq JsonQuery]))

(defonce object-mapper (ObjectMapper.))

(defn read-tree* [json-string]
  (.readTree object-mapper json-string))

(def
  ^{:arglists '([json-string])
    :doc "Convert a `json-string` into a `JsonNode` instance that can be used to query against. Caches results."}
  read-tree
  (fifo read-tree* :fifo/threshold 1000))

(defn jq-compile* [jq-query-string]
  (JsonQuery/compile jq-query-string))

(def
  ^{:arglists '([jq-query-string])
    :doc "Compile supplied `jq-query-string` and return `JsonQuery` object. Caches results."}
  jq-compile
  (fifo jq-compile* :fifo/threshold 1000))

(defn jq-apply-query*
  [json-string jq-query-string]
  (let [node (read-tree json-string)
        query (jq-compile jq-query-string)]
    (mapv (comp json/parse-string str) (.apply query node))))

(def
  ^{:arglists '([json-string jq-query-string])
    :doc "Apply a jq query to JSON, returning the results as Clojure data structures. Caches results."}
  jq-apply-query
  (fifo jq-apply-query* :fifo/threshold 1000))

(defn jq-str
  "Given a JSON string and a jq query string, return sequence of results as Clojure data."
  [json-string jq-query-string]
  (jq-apply-query json-string jq-query-string))

(defn jq
  "Given a Clojure map and a jq query string, return sequence of results as Clojure data."
  [m jq-query-string]
  (jq-apply-query (json/generate-string m) jq-query-string))
