(require 'cljs.build.api)

(cljs.build.api/build "src"
  {:output-to "target/thrash-main.js"
   :optimizations :simple})

(System/exit 0)