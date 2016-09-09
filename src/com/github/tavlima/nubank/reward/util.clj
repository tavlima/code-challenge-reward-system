(ns com.github.tavlima.nubank.reward.util
  (require [clojure.pprint :as pprint]))

(defn exp [x n]
  "Calculates x^n. 'n' must be an integer."
  (loop [acc 1 n n]
    (if (zero? n) acc
                  (recur (* x acc) (dec n)))))

(defn str->int
  [str]
  (Integer. str))