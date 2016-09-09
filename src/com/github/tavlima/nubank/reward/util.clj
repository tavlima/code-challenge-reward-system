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

(defn is-integer? [s]
  (if ((complement nil?) s)
    (if (integer? s)
      true
      (if (string? s)
        (if-let [s (seq s)]
          (let [s (if (= (first s) \-) (next s) s)
                s (drop-while #(Character/isDigit %) s)]
            (empty? s)))
        false))
    false))