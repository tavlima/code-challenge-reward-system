(ns com.github.tavlima.nubank.reward.tree.domain.t-zippable
  (:require [com.github.tavlima.nubank.reward.tree.domain.zippable :as z]
            [com.github.tavlima.nubank.reward.tree.domain.location :as l])
  (:import (clojure.lang Keyword PersistentArrayMap)))

(extend-protocol z/Zippable
  PersistentArrayMap
  (children [m] (get m :c []))
  (make [m c] (assoc m :c c))
  (zipper [m] (l/->LocationImpl m [:nil] [:nil] [:nil])))

(extend-protocol z/Zippable
  Keyword
  (children [_] (list))
  (make [key _] key)
  (zipper [key]
    (l/->LocationImpl key [:nil] [:nil] [:nil])))