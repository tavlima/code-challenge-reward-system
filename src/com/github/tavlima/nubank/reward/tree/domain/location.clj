(ns com.github.tavlima.nubank.reward.tree.domain.location
  (:require [com.github.tavlima.nubank.reward.tree.domain.zippable :as z])
  (:refer-clojure :exclude [update replace next]))

(defprotocol Location
  "Represents a location in an agnostic tree."
  (node [loc])
  (end? [loc])
  (replace [loc node])
  (update [loc transform])
  (down [loc])
  (right [loc])
  (next [loc])
  (up [loc])
  (root [loc])
  (append-child [loc child]))

(defrecord LocationImpl [node path s-left s-right]
  Location
  (node [_] node)

  (end? [_] (= node :nil))

  (replace [_ newNode]
    (->LocationImpl newNode path s-left s-right))

  (update [_ transform]
    (->LocationImpl (transform node) path s-left s-right))

  (append-child [_ child]
    (-> (z/children node)
        (conj child)
        (#(z/make node %))
        (->LocationImpl path s-left s-right)))

  (down [_]
    (let [[child & others] (z/children node)]
      (if child
        (->LocationImpl child
                        (cons node path)
                        [s-left]
                        (conj (into [] others) s-right))
        nil)))

  (right [_]
    (let [[sibling & others] s-right]
      (if ((complement vector?) sibling)
        (->LocationImpl sibling path (conj s-left node) (into [] others))
        nil)))

  (up [_]
    (let [[parent & others] path]
      (if (= :nil parent)
        nil
        (let [[parent-left & siblings-left] s-left
              siblings-right (into [] (butlast s-right))
              parent-right (last s-right)
              new-parent-children (concat (conj (into [] siblings-left) node) siblings-right)
              new-node (z/make parent new-parent-children)
              new-path (into [] others)]
          (->LocationImpl new-node new-path parent-left parent-right)))))

  (root [loc]
    (loop [curLoc loc]
      (let [parent (up curLoc)]
        (if (nil? parent)
          curLoc
          (recur parent)))))

  (next [loc]
    (or (down loc)
        (right loc)
        (loop [parentLoc (up loc)]
          (if (end? parentLoc)
            nil
            (let [uncleLoc (right parentLoc)]
              (if uncleLoc
                uncleLoc
                (recur (up parentLoc)))))))))
