(ns com.rentpath.jj.repl-test
  (:require [com.rentpath.jj.repl :as sut]
            [clojure.test :as t]))

(t/deftest test-read-def
  (t/is (= [:jj/def 'foo {"answer" 42}]
           (sut/read-def "def foo parse \"https://gist.githubusercontent.com/semperos/a55ffd57e1ca8b0406e415176a22b9c1/raw/c2816aeffb75e669d36614d34861b9a29361ecbd/example.json\""))))
