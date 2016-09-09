(ns com.github.tavlima.nubank.reward.util
  (require [clojure.pprint :as pprint]))

(defn exp [x n]
  "Calculates x^n. 'n' must be an integer."
  (loop [acc 1 n n]
    (if (zero? n) acc
                  (recur (* x acc) (dec n)))))

(defn map-kv [m f]
  (reduce-kv #(assoc %1 %2 (f %3)) {} m))

(defn truncate [precision value]
  (Float. (pprint/cl-format nil (str "~," precision "f") value)))

(defn str->int
  [str]
  (Integer. str))