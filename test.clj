(println "Hello, Clojure!")
;; print out any command line arguments
(doall
  (map #(println (str "Arg #" (first %) ": " (last %)))
    (map vector (iterate inc 1) *command-line-args*)))
