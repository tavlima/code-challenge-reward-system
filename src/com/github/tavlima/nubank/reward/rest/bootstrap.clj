(ns com.github.tavlima.nubank.reward.rest.bootstrap
  (:require [clojure.java.io :as io]
            [com.github.tavlima.nubank.reward.rest.adapter-invitations :as adapter]))

(defn process-line [_ line]
  (apply adapter/invite (clojure.string/split line #"\s+")))

(defn process-file [filename]
  (with-open [reader (io/reader filename)]
    (reduce process-line nil (line-seq reader))))