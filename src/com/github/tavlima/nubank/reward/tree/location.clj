(ns com.github.tavlima.nubank.reward.tree.location
  (:refer-clojure :exclude [update replace next]))

(declare cloneLoc)

(defprotocol ILocation
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

(defprotocol ILocatable
  "Helper protocol for the Location extension
  of the ILocation protocol"
  (children [node])
  (make [node children])
  (zipper [node]))

#_(extend-protocol ILocatable
  Keyword
  (children [node]
    (if (= node :nil) [] (throw IllegalArgumentException)))
  (make [node _]
    (if (= node :nil) :nil (throw IllegalArgumentException))))

(defrecord Location [node path s-left s-right]
  ILocation
  (node [_] node)

  (end? [_] (= node :nil))

  (replace [_ newNode]
    (->Location newNode path s-left s-right))

  (update [_ transform]
    (->Location (transform node) path s-left s-right))

  (append-child [_ child]
    (-> (children node)
        (conj child)
        (#(make node %))
        (->Location path s-left s-right)))

  (down [_]
    (let [[child & others] (children node)]
      (if child
        (->Location child
                    (cons node path)
                    [s-left]
                    (conj (into [] others) s-right))
        nil)))

  (right [_]
    (let [[sibling & others] s-right]
      (if ((complement vector?) sibling)
        (->Location sibling path (conj s-left node) (into [] others))
        nil)))

  (up [_]
    (let [[parent & others] path]
      (if (= :nil parent)
        nil
        (let [[parent-left & siblings-left] s-left
              siblings-right (into [] (butlast s-right))
              parent-right (last s-right)
              new-parent-children (concat (conj (into [] siblings-left) node) siblings-right)
              new-node (make parent new-parent-children)
              new-path (into [] others)]
          (->Location new-node new-path parent-left parent-right)))))

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