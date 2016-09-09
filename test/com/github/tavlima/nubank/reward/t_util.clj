(ns com.github.tavlima.nubank.reward.t-util
  (:use clojure.test
        midje.sweet
        com.github.tavlima.nubank.reward.util))

(facts "about `str->int`"
       (fact "it converts a string to int"
             (str->int "1") => 1
             (str->int "2") => 2
             (str->int "10") => 10))

(facts "about `truncate`"
       (fact "it truncates a float 'value' to 'precision' digits"
             (truncate 2 1/3) => (float 0.33)
             (truncate 2 0.159) => (float 0.16)
             (truncate 5 10/3) => (float 3.33333)))

(facts "about `exp`"
       (fact "it calculates x^n"
             (exp 2 2) => 4
             (exp 2 3) => 8
             (exp 1/2 2) => 1/4
             (exp 1/4 0) => 1))

(facts "about `map-kv`"
       (fact "it applies the function f to each of the 'm' map values and returns the updated map"
             (map-kv {:a 1} inc) => {:a 2}
             (map-kv {:a 1 :b 2} dec) => {:a 0 :b 1}
             (map-kv {:a 1 :b 2} #(str "value_" %)) => {:a "value_1" :b "value_2"}))