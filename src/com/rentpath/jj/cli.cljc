(ns com.rentpath.jj.cli
  (:require [clojure.string :as str]
            #?(:clj [clojure.java.io :as io])
            #?(:cljs [clojure.walk :refer [postwalk]])
            #?(:clj [clojure.tools.cli :refer [parse-opts]])
            #?(:cljs [cljs.tools.cli :refer [parse-opts]])
            [com.rentpath.jj :as jj]
            #?(:cljs [cljs.nodejs :as node])
            #?(:clj [cheshire.core :as json])
            #?(:cljs [cognitect.transit :as t])
            [com.rentpath.jj.elasticsearch :as es]
            [com.rentpath.jj.lang :as lang])
  #?(:clj (:gen-class)))

#?(:cljs (def fs (js/require "fs")))
#?(:cljs (def process (js/require "process")))
#?(:cljs (node/enable-util-print!))
#?(:cljs (.on process "uncaughtException" #(js/console.error %)))
#?(:cljs (defn symbolize-keys
           "Recursively transforms all map keys from strings to symbols."
           [m]
           (let [f (fn [[k v]] (if (string? k) [(symbol k) v] [k v]))]
             ;; only apply to maps
             (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))

(def envs
  {:default lang/default-env
   :es es/v6-env
   :elasticsearch es/v6-env})

#?(:clj (defn file-exists? [file-name]
          (when-let [f (io/as-file file-name)]
            (.exists f))))

#?(:cljs (defn file-exists? [file-name]
           (.existsSync fs file-name)))

(def cli-options
  [["-m" "--mode MODE" "Mode to use, adding shortcut symbols to jj."
    :default "default"
    :validate [#(or (find envs (keyword %)) (file-exists? %))
               (str "Must be a path to a mode JSON file or one of: " (pr-str (map name (keys envs))))]]
   ;; A non-idempotent option
   ;; ["-v" nil "Verbosity level"
   ;;  :id :verbosity
   ;;  :default 0
   ;;  :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["jj utility for producing JSON concisely."
        ""
        "Usage: jj [options] program"
        ""
        "Options:"
        options-summary
        ""
        "Available modes:"
        (map #(str "  - "(name %)) (keys envs))]
       flatten
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while processing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary] :as validated} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}

      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}

      (zero? (count arguments))
      {:exit-message (str (usage summary) "\n\nPlease supply a jj program. For example: a:b")}

      ;; custom validation on arguments
      :else
      {:program (str/join " " arguments) :options options})))

(defn exit [status msg]
  (println msg)
  #?(:clj (System/exit status)
     :cljs (.exit process status)))

#?(:clj (defn parse-mode-file
          [file-name]
          (json/parse-string (slurp file-name) symbol)))

#?(:cljs (defn parse-mode-file
           [file-name]
           (let [r (t/reader :json)]
             (symbolize-keys (t/read r (str (.readFileSync fs file-name {:encoding "utf8"})))))))

(defn determine-mode
  "Allow user-specified modes via file, allowing them to create custom modes or overridden versions of built-in ones."
  [mode]
  (if (file-exists? mode)
    (parse-mode-file mode)
    (get envs (keyword mode) lang/default-env)))

(defn -main [& args]
  (let [{:keys [program options exit-message ok?] :as parsed } (validate-args args)
        mode (determine-mode (:mode options))]
    (assert (and (map? mode)
                 (every? symbol? (keys mode)))
            (str "The mode must be an environment map of symbols to symbols. Found: " (pr-str mode)))
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (binding [lang/*env* mode]
        (jj/pj program)))))

#?(:cljs (set! *main-cli-fn* -main))
