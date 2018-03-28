(ns com.rentpath.jj.repl
  (:require [clojure.main]
            [rebel-readline.core]
            [rebel-readline.clojure.line-reader]
            [rebel-readline.clojure.service.local]
            [rebel-readline.tools :as tools]
            [com.rentpath.jj :as jj]
            [com.rentpath.jj.jq :as jq]
            [com.rentpath.jj.lang :as lang]
            [com.rentpath.jj.parser :refer [parse]]
            [com.rentpath.jj.elasticsearch :as es]
            [cheshire.core :as json]
            [clojure.string :as str])
  (:gen-class))

(defmethod tools/-prompt :rebel-readline.clojure.line-reader/clojure [service]
  "jj=> ")

(def newlines (atom 0))

(defn exit [status] (System/exit status))

(defn skip-if-eol
  "If the next character on stream s is a newline, skips it, otherwise
  leaves the stream untouched. Returns :line-start, :stream-end, or :body
  to indicate the relative location of the next character on s. The stream
  must either be an instance of LineNumberingPushbackReader or duplicate
  its behavior of both supporting .unread and collapsing all of CR, LF, and
  CRLF to a single \\newline."
  [s]
  (let [c (.read s)]
    (cond
      (= c (int \newline)) :line-start
      (= c -1) :stream-end
      :else (do (.unread s c) :body))))

(defn read-entry
  [s]
  (loop [c (.read s) output ""]
    (cond
      (= c (int \newline))
      (do (.unread s c) output)

      (= c -1)
      (do
        (if (empty? output)
          (do (println "You pressed Ctrl-D, didn't you?\nTry jj/quit next time.")
              (flush)
              (exit 0))
          output))

      :else
      (recur (.read s) (str output (char c))))))

(declare handle-special-input)
(defn read-def
  [raw-input]
  (let [sans-def (str/triml (subs raw-input 4))
        [identifier expression-str] (->> sans-def
                                         (split-with #(not (or (Character/isWhitespace %) (= % \,))))
                                         (map (partial apply str)))
        expression (->> (java.io.ByteArrayInputStream. (.getBytes expression-str))
                        read-entry
                        str/trim
                        handle-special-input)]
    [:jj/def (symbol identifier) expression]))

(def jj-program (atom ""))

(defn handle-special-input
  [raw-input]
  (cond
    (= raw-input "jj/quit")
    (do
      (println "You're right, you should be writing Clojure.")
      (flush)
      (exit 0))

    (= raw-input "jj/reset")
    (do
      (reset! jj-program "")
      (alter-var-root #'lang/*env* (constantly {}))
      "jj/reset:true")

    (#{"jj/elasticsearch-mode" "jj/es-mode"} raw-input)
    (do
      (alter-var-root #'lang/*env* (constantly es/v6-env))
      "jj/mode:elasticsearch")

    (#{"jj/restore-defaults" "jj/default-mode"} raw-input)
    (do
      (alter-var-root #'lang/*env* (constantly {}))
      "jj/mode:default")

    (str/starts-with? raw-input "def ")
    (read-def raw-input)

    (str/starts-with? raw-input "parse ")
    (let [raw-json (subs raw-input 6)
          str-length (count raw-json)
          stripped (subs (subs raw-json 1) 0 (- str-length 2))
          s  (try
               (slurp stripped)
               (catch Exception _
                 (str/replace stripped "\\\"" "\"")))]
      (json/parse-string s))

    :else raw-input))

(defn repl-read
  "Default :read hook for repl. Reads from *in* which must either be an
  instance of LineNumberingPushbackReader or duplicate its behavior of both
  supporting .unread and collapsing all of CR, LF, and CRLF into a single
  \\newline. repl-read:
    - skips whitespace, then
      - returns request-prompt on start of line, or
      - returns request-exit on end of stream, or
      - reads an object from the input stream, then
        - skips the next input character if it's end of line, then
        - returns the object."
  [request-prompt request-exit]
  (let [raw-input (read-entry *in*)
        raw-input (handle-special-input raw-input)
        input (try
                (let [ret (if (string? raw-input)
                            (parse (str @jj-program raw-input))
                            ;; Special forms can return Clojure data at read time,
                            ;; see handle-special-input
                            [raw-input])]
                  (reset! jj-program "")
                  ret)
                (catch Exception e
                  (if (= (:failure (ex-data e) "instaparse.gll.Failure"))
                    (do
                      (swap! jj-program str raw-input)
                      ::ignore)
                    ::ignore)))]
    (skip-if-eol *in*)
    input))

(defn eval-def
  [[_ identifier expression :as arg]]
  (alter-var-root #'lang/*env* assoc identifier (jj/eval-jj expression)))

(defn eval-jq [x]
  (let [[jj-json jq-query] (split-with (partial not= 'jq) x)
        jq-query (first (next jq-query))]
    (first (jq/jq (jj/eval-jj jj-json) jq-query))))

(defn repl-eval [x]
  (if (= x ::ignore)
    x
    (try
      (cond
        (some #{'jq} x) (eval-jq x)
        (and (coll? (first x))
             (= (ffirst x) :jj/def)) (eval-def (first x))
        :else (jj/eval-jj x))
      (catch Exception e
        (.printStackTrace e)
        (throw e)))))

(defn repl-print [x]
  (if (= x ::ignore)
    (print)
    (try
      (println (json/generate-string x {:pretty true}))
      (catch Exception _
        (println "Can't JSON encode:" (pr-str x))))))

(defn repl []
  (rebel-readline.core/with-readline-in
    (rebel-readline.clojure.line-reader/create
     (rebel-readline.clojure.service.local/create))
    (clojure.main/repl :prompt (fn [])
                       :read   repl-read
                       :eval   repl-eval
                       :print  repl-print)))

(defn -main [& args]
  (repl))
