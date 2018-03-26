(ns com.rentpath.jj.cli
  (:require [clojure.string :as str]
            #?(:clj [clojure.tools.cli :refer [parse-opts]])
            #?(:cljs [cljs.tools.cli :refer [parse-opts]])
            [com.rentpath.jj :as jj]
            #?(:cljs [cljs.nodejs :as node])
            [com.rentpath.jj.elasticsearch :as es]
            [com.rentpath.jj.lang :as lang])
  #?(:clj (:gen-class)))

#?(:cljs (def process (js/require "process")))
#?(:cljs (node/enable-util-print!))
#?(:cljs (.on process "uncaughtException" #(js/console.error %)))

(def envs
  {:default lang/default-env
   :es es/v6-env
   :elasticsearch es/v6-env})

(def cli-options
  [["-m" "--mode MODE" "Mode to use, adding shortcut symbols to jj."
    :default "default"
    :parse-fn keyword
    :validate [#(find envs %) (str "Must be one of: " (pr-str (map name (keys envs))))]]
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

(defn -main [& args]
  (let [{:keys [program options exit-message ok?] :as parsed } (validate-args args)
        mode (get envs (:mode options) lang/default-env)]
    (assert (map? mode) "The mode must be an environment map of symbols to symbols.")
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (binding [lang/*env* mode]
        (jj/pj program)))))

#?(:cljs (set! *main-cli-fn* -main))
