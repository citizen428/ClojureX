#!/usr/bin/env clj
(println "Hello, Clojure!")
;; print out any command line arguments
(doall
  (map #(println (str "Arg #" %1 ": " %2))
        (iterate inc 1) *command-line-args*))
