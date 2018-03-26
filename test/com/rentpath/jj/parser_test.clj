(ns com.rentpath.jj.parser-test
  (:require [com.rentpath.jj.parser :as sut]
            [clojure.test :as t]
            [com.rentpath.jj.elasticsearch :as es]
            [com.rentpath.jj.lang :as lang]))

(t/deftest test-parse-basics
  (t/are [s res] (= res (first (sut/parse s)))
    "false"   false
    "null"    nil
    "true"    true
    "42"      42
    "42.23"   42.23
    "abc"     'abc
    "\"abc\"" "abc"))

(t/deftest test-parse-object
  (t/are
      [s res]
      (binding [lang/*env* es/v6-env]
        (= res (first (sut/parse s))))
    "__s:3"
    '{__s 3}

    "q: b: m: t: searchpath: \"CITY\""
    '{query {bool {must {term {searchpath "CITY"}}}}}

    "_s:i:[\"listingid\" \"propertyname\" \"searchonly\"],
     q:b:m:[gd:{d:\"30miles\",
                geocode:{lat:33.762622,lon:-84.422791}
                _name: \"distance\"}]"
    '{_source {includes ["listingid" "propertyname" "searchonly"]}
      query {bool
             {must
              [{geo_distance
                {distance "30miles"
                 geocode {lat 33.762622 lon -84.422791}
                 _name "distance"}}]}}}))

(t/deftest test-parse-array
  (t/are [s res] (= res (first (sut/parse s)))
    "[1 2 3]" [1 2 3]
    "[\"a\" \"b\" \"c\"]" ["a" "b" "c"]))
