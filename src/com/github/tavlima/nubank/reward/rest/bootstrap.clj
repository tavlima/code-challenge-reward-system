(ns com.github.tavlima.nubank.reward.rest.bootstrap
  (:require [clojure.java.io :as io]
            [com.github.tavlima.nubank.reward.rest.adapter-invitations :as adapter]
            [com.github.tavlima.nubank.reward.util :as util]))

(defn process-line [_ line]
  (apply adapter/invite
         (map util/str->int (clojure.string/split line #"\s+"))))

(defn process-file [filename]
  (with-open [reader (io/reader (io/resource filename))]
    (reduce process-line nil (line-seq reader))))