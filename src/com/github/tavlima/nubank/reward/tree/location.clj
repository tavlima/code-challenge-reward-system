(ns com.github.tavlima.nubank.reward.tree.location
  (:refer-clojure :exclude [update replace])
  (:import (clojure.lang Keyword)))

(declare cloneLoc)

(defprotocol ILocation
  "Represents a location in an agnostic tree."
  (node [loc])
  (end? [loc])
  (replace [loc node])
  (update [loc transform])
  (goDown [loc])
  (goRight [loc])
  (goNext [loc])
  (goUp [loc])
  (root [loc])
  (append-child [loc child]))

(defprotocol ILocatable
  "Helper protocol for the Location extension
  of the ILocation protocol"
  (children [node])
  (make [node children])
  (zipper [node]))

(extend-protocol ILocatable
  Keyword
  (children [node]
    (if (= node :nil) [] (throw IllegalArgumentException)))
  (make [node _]
    (if (= node :nil) :nil (throw IllegalArgumentException))))

(defrecord Location [node path left right]
  ILocation
  (node [_] node)

  (end? [_] (= node :nil))

  (replace [_ newNode]
    (->Location newNode path left right))

  (update [_ transform]
    (->Location (transform node) path left right))

  (append-child [_ child]
    (-> (children node)
        (conj child)
        (#(make node %))
        (->Location path left right)))

  (goDown [_]
    (let [[child & others] (children node)]
      (if child
        (->Location child
                    (cons node path)
                    [left]
                    (conj (into [] others) right))
        nil)))

  (goRight [_]
    (let [[sibling & others] right]
      (if ((complement vector?) sibling)
        (->Location sibling path (conj left node) (into [] others))
        nil)))

  (goUp [_]
    (let [[parent & others] path]
      (if (= :nil parent)
        nil
        (let [[parent-left & siblings-left] left
              siblings-right (into [] (butlast right))
              parent-right (last right)
              new-parent-children (concat (conj (into [] siblings-left) node) siblings-right)
              new-node (make parent new-parent-children)
              new-path (into [] others)]
          (->Location new-node new-path parent-left parent-right)))))

  (root [loc]
    (loop [curLoc loc]
      (let [parent (goUp curLoc)]
        (if (nil? parent)
          curLoc
          (recur parent)))))

  (goNext [loc]
    (or (goDown loc)
        (goRight loc)
        (loop [parentLoc (goUp loc)]
          (if (end? parentLoc)
            nil
            (let [uncleLoc (goRight parentLoc)]
              (if uncleLoc
                uncleLoc
                (recur (goUp parentLoc)))))))))