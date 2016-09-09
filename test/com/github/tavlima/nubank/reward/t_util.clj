(ns com.github.tavlima.nubank.reward.t-util
  (:use clojure.test
        midje.sweet
        com.github.tavlima.nubank.reward.util))

(facts "about `str->int`"
       (fact "it converts a string to int"
             (str->int "1") => 1
             (str->int "2") => 2
             (str->int "10") => 10))

(facts "about `exp`"
       (fact "it calculates x^n"
             (exp 2 2) => 4
             (exp 2 3) => 8
             (exp 1/2 2) => 1/4
             (exp 1/4 0) => 1))
