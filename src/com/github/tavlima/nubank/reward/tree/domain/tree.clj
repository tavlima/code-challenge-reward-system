(ns com.github.tavlima.nubank.reward.tree.domain.tree
  (:require [com.github.tavlima.nubank.reward.tree.domain
             [treenode :as n]
             [zippable :as z]
             [location :as l]])
  (:refer-clojure :exclude [empty?]))

(defprotocol Tree
  (empty? [tree])
  (add [tree uid])
  (replaceRoot [tree node])
  (containsUid? [tree uid])
  (nodes [tree ks])
  (findFirst [tree uid])
  (findFirstByMatcher [tree matcher]))

(defrecord TreeImpl [root uids]
  Tree
  (empty? [_] (and (nil? root) (clojure.core/empty? uids)))
  (add [tree uid] (assoc tree :uids (conj uids uid)))
  (replaceRoot [tree node] (assoc tree :root node))
  (containsUid? [_ key] (contains? uids key))
  (findFirstByMatcher [_ matcher]
    (if (nil? root)
      nil
      (loop [curLoc (z/zipper root)]
        (cond (l/end? curLoc) nil
              (matcher (l/node curLoc)) curLoc
              :else (recur (l/next curLoc))))))
  (findFirst [tree uid]
    (findFirstByMatcher tree #(n/match? % uid)))
  (nodes [_ ks]
    (let [zipper (z/zipper root)
          iterator (iterate l/next zipper)
          locs (take-while (complement l/end?) iterator)
          nodes (map l/node locs)
          nodesFields (map #(n/fields % ks) nodes)]
      nodesFields)))

(defn create-tree []
  (->TreeImpl nil #{}))
