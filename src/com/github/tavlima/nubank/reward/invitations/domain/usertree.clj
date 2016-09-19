(ns com.github.tavlima.nubank.reward.invitations.domain.usertree
  (:require [com.github.tavlima.nubank.reward.tree.domain.tree :as t])
  (:import (com.github.tavlima.nubank.reward.tree.domain.tree TreeImpl)))

(defprotocol UserTree
  (users [tree]))

(extend-protocol UserTree
  TreeImpl
  (users [tree]
    (t/nodes tree [:id :score :verified])))
