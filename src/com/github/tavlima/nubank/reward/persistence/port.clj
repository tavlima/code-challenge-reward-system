(ns com.github.tavlima.nubank.reward.persistence.port
  (:require [com.github.tavlima.nubank.reward.persistence.controller :as controller]))

(defn get-tree
  "Gets the current tree"
  []
  (controller/get-tree))

(defn update-tree
  "Applies f to the current tree, like (f current-tree),
  and stores the value returned by this call as the updated tree."
  [f]
  (controller/update-tree f))