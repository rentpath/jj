(ns com.rentpath.jj-test
  (:require [clojure.test :as t]
            ;; [clojure.spec.alpha :as s]
            ;; [clojure.spec.test.alpha :as stest]
            [com.rentpath.jj :as sut]
            [com.rentpath.jj.elasticsearch :as es]
            [com.rentpath.jj.lang :as lang]))

;; (stest/instrument `sut/parse-number)
;; (stest/instrument `sut/parse-string)

(t/deftest test-deep-merge-for-multi-jj-programs
  (t/are
      [s res]
      (binding [lang/*env* es/v6-env]
        (= res (sut/jj s)))
    "q:b:m:[gd:d:\"30mi\"] q:b:s:[gs:bs:10]"
    '{query {bool {must [{geo_distance {distance "30mi"}}]
                   should [{geo_shape {boost 10}}]}}}

    "
q:b:m:[gd:d:\"30mi\"]
q:b:m:[gd:geocode:{lat:33.762622,lon:-84.422791}]
q:b:m:[gd:_name:distance]
q:b:s:[gs:geoshape:{r:w,bs:10,_name:shape} {}]
q:b:s:[gs:geoshape:indexed_shape:{index:city_shape,type:doc,id:\"1304000\",path:geometry} {}]
q:b:s:[{} t:searchonly:{value:0,boost:10,_name:searchonly}]
_s:i:[listingid propertyname searchonly sortorder]
st:{searchonly:asc,_score:desc,sortorder:asc}
"
    '{query {bool {must [{geo_distance {distance "30mi"
                                        geocode {lat 33.762622 lon -84.422791}
                                        _name distance}}]
                   should [{geo_shape
                            {geoshape
                             {relation within
                              boost 10
                              _name shape
                              indexed_shape {index city_shape
                                             type doc
                                             id "1304000"
                                             path geometry}}}}
                           {term
                            {searchonly
                             {value 0
                              boost 10
                              _name searchonly}}}]}}
      _source {includes [listingid propertyname searchonly sortorder]}
      sort {searchonly asc
            _score desc
            sortorder asc}}))
