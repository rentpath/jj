(ns com.rentpath.jj.util)

#?(:cljs (def process (js/require "process")))

(defn handle-args [args]
  (when (empty? args)
    (println "Please supply a jj program. For example: a:b")
    #?(:clj (System/exit 1)
       :cljs (.exit process 1)))
  args)

;; Derived from https://github.com/circleci/frontend/blob/04701bd314731b6e2a75c40085d13471b696c939/src-cljs/frontend/utils.cljs
(defn deep-merge* [& maps]
  (let [f (fn [old new]
            (if (and (map? old) (map? new))
              (merge-with deep-merge* old new)
              new))]
    (cond
      (every? map? maps)    (apply merge-with f maps)
      (every? vector? maps) (apply mapv (fn [a b] (merge-with f a b)) maps)
      :else                 (last maps))))

;; Derived from https://github.com/circleci/frontend/blob/04701bd314731b6e2a75c40085d13471b696c939/src-cljs/frontend/utils.cljs
(defn deep-merge
  [& maps]
  (let [maps (filter identity maps)]
    (assert (every? map? maps))
    (apply merge-with deep-merge* maps)))
