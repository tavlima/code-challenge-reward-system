(ns com.github.tavlima.nubank.reward.util)

(defn exp [x n]
  "Calculates x^n. 'n' must be an integer."
  (loop [acc 1 n n]
    (if (zero? n) acc
                  (recur (* x acc) (dec n)))))
