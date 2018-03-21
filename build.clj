(require 'cljs.build.api)

(cljs.build.api/build "src" {:main 'com.rentpath.jj.cli
                             :output-to "out/jj"
                             :target :nodejs
                             :optimizations :advanced})
