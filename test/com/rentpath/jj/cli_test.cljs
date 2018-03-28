(ns com.rentpath.jj.cli-test
  (:require [com.rentpath.jj.cli :as sut]
            [cljs.test :as t :include-macros true]))

(t/deftest test-symbolize-keys
  (t/is (= {'alpha :beta 'gamma "delta" :epsilon 'zeta}
           (sut/symbolize-keys {"alpha" :beta "gamma" "delta" :epsilon 'zeta}))))

(t/deftest test-file-exists?
  (t/is (sut/file-exists? "LICENSE"))
  (t/is (not (sut/file-exists? "App.java"))))
