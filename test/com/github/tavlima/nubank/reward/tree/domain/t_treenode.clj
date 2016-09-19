(ns com.github.tavlima.nubank.reward.tree.domain.t-treenode
  (:require [com.github.tavlima.nubank.reward.tree.domain.treenode :as n])
  (:import (clojure.lang IPersistentMap)))

(extend-protocol n/TreeNode
  IPersistentMap
  (match? [m v] (= (:v m) v))
  (uid [m] (:v m))
  (fields [m ks] (select-keys m ks)))
