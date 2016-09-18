(ns com.github.tavlima.nubank.reward.tree.domain
  (require [com.github.tavlima.nubank.reward.tree.location :as loc]))

;; TreeNode
(defprotocol ITreeNode
  (match? [node value])
  (uid [node])
  (fields [node ks]))

;; User
(defprotocol IUser
  (addScore [user delta])
  (verified? [user])
  (verify! [user])
  (addInvited [user invited]))

;; Tree
(defprotocol ITree
  (add [tree uid])
  (replaceRoot [tree node])
  (containsUid? [tree uid])
  (nodes [tree] [tree ks])
  (findFirst [tree uid])
  (findFirstByMatcher [tree matcher]))

(defrecord User [id score invited verified]
  ITreeNode
  (match? [_ matchId] (= id matchId))
  (uid [_] id)
  (fields [node ks]
    (select-keys node ks))

  loc/ILocatable
  (children [_] invited)
  (make [user children] (assoc user :invited children))
  (zipper [user] (loc/->Location user [:nil] [:nil] [:nil]))

  IUser
  (addScore [user delta] (assoc user :score (+ score delta)))
  (verified? [_] verified)
  (verify! [user] (assoc user :verified true))
  (addInvited [user invitedUser] (assoc user :invited (conj invited invitedUser))))

(defrecord Tree [root uids]
  ITree
  (add [tree uid] (assoc tree :uids (conj uids uid)))
  (replaceRoot [tree node] (assoc tree :root node))
  (containsUid? [_ key] (contains? uids key))
  (findFirstByMatcher [_ matcher]
    (if (nil? root)
      nil
      (loop [curLoc (loc/zipper root)]
        (cond (loc/end? curLoc) nil
              (matcher (loc/node curLoc)) curLoc
              :else (recur (loc/goNext curLoc))))))
  (findFirst [tree uid]
    (findFirstByMatcher tree #(match? % uid)))
  (nodes [tree]
    (nodes tree [:id :score :verified]))
  (nodes [_ ks]
    (let [zipper (loc/zipper root)
          iterator (iterate loc/goNext zipper)
          locs (take-while (complement loc/end?) iterator)
          nodes (map loc/node locs)
          nodesFields (map #(fields % ks) nodes)]
      nodesFields)))

(defn create-user [id]
  (->User id 0 [] false))

(defn create-tree []
  (->Tree nil #{}))