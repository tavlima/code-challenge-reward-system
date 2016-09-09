(ns com.github.tavlima.nubank.reward.util)

(defn exp [x n]
  "Calculates x^n. 'n' must be an integer."
  (loop [acc 1 n n]
    (if (zero? n) acc
                  (recur (* x acc) (dec n)))))

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

(defn decode
  [data]
  (cond ((complement string?) data)
        data

        (is-integer? data)
        (Integer. data)

        (= (first data) \:)
        (keyword (apply str (rest data)))

        :else data))
