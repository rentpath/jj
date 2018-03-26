(ns com.rentpath.jj.cli-test
  (:require [com.rentpath.jj.cli :as sut]
            [clojure.test :as t]
            [cheshire.core :as json]))

(defmacro out-json
  [& body]
  `(json/parse-string
    (with-out-str ~@body)
    true ;; keyword keys
    ))

(t/deftest test-cli-main
  (t/is (= {:a {:b "c"}}
           (out-json (sut/-main "a:b:c"))))
  (t/is (= {:a {:b {:c {:d "e"
                        :x "y"}}}}
           (out-json (sut/-main "a:b:c:d:e" "a:b:c:x:y"))))
  (t/is (= {:aggs {:bool "c"}}
           (out-json (sut/-main "-m" "es" "a:b:c"))))
  (t/is (= {:aggs {:bool "c"}}
           (out-json (sut/-main "-m" "elasticsearch" "a:b:c"))))
  (t/is (= {:aggs {:bool "c"}}
           (out-json (sut/-main "--mode" "elasticsearch" "a:b:c")))))
