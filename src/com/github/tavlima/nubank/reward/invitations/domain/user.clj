(ns com.github.tavlima.nubank.reward.invitations.domain.user
  (:require [com.github.tavlima.nubank.reward.tree.domain
             [location :as l]
             [treenode :as n]
             [zippable :as z]]))

(defprotocol User
  (addScore [user delta])
  (verified? [user])
  (verify! [user])
  (addInvited [user invited]))

(defrecord UserImpl [id score invited verified]
  n/TreeNode
  (match? [_ matchId] (= id matchId))
  (uid [_] id)
  (fields [node ks]
    (select-keys node ks))

  z/Zippable
  (children [_] invited)
  (make [user children] (assoc user :invited children))
  (zipper [user] (l/->LocationImpl user [:nil] [:nil] [:nil]))

  User
  (addScore [user delta] (assoc user :score (+ score delta)))
  (verified? [_] verified)
  (verify! [user] (assoc user :verified true))
  (addInvited [user invitedUser] (assoc user :invited (conj invited invitedUser))))

(defn create-user [id]
  (->UserImpl id 0 [] false))
