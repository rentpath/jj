(ns com.rentpath.jj.cli-test
  (:require [com.rentpath.jj.cli :as sut]
            [clojure.test :as t]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defmacro out-json
  [& body]
  `(json/parse-string
    (with-out-str ~@body)
    true ;; keyword keys
    ))

(defn test-main-input
  "Tests by passing jj programs in both directly and via stdin"
  [expected & main-args]
  (let [main-args (vec main-args)
        idx (.indexOf main-args ::stdin)
        direct-args (concat (subvec main-args 0 idx) (subvec main-args (inc idx)))
        stdin-args (concat (take-while (partial not= ::stdin) main-args) ["-"])
        stdin-str (str/join " " (next (drop-while (partial not= ::stdin) main-args)))]
    (t/is (= expected (out-json (apply sut/-main direct-args))))
    (binding [*in* (java.io.ByteArrayInputStream. (.getBytes stdin-str "UTF-8"))]
      (t/is (= expected (out-json (apply sut/-main stdin-args)))))))

(t/deftest test-cli-main
  (with-redefs [sut/exit (fn [status])]
    (test-main-input {:a {:b "c"}} ::stdin "a:b:c")
    (test-main-input
     {:a {:b {:c {:d "e"
                  :x "y"}}}}
     ::stdin "a:b:c:d:e" "a:b:c:x:y")
    (test-main-input {:aggs {:bool "c"}}
                     "-m" "es" ::stdin "a:b:c")
    (test-main-input {:aggs {:bool "c"}}
                     "-m" "elasticsearch" ::stdin "a:b:c")
    (test-main-input {:aggs {:bool {:c "distance"
                                    :y "z"}}}
                     "--mode" "elasticsearch" "a:b:y:z" ::stdin "a:b:c:d")))
