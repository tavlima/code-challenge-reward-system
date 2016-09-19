(ns com.github.tavlima.nubank.reward.invitations.controller
  (require [com.github.tavlima.nubank.reward.tree.domain
            [location :as z]
            [tree :as t]]
           [com.github.tavlima.nubank.reward.invitations.domain
            [user :as u]
            [usertree :as ut]]
           [com.github.tavlima.nubank.reward
            [util :as util]
            [compare :as compare]]))

(defn- ^{:testable true} update-score [loc level]
  (if (>= level 0)
    (z/update loc #(u/addScore % (util/exp 1/2 level)))
    loc))

(defn- ^{:testable true} update-parents-scores [zipper]
  (loop [loc zipper
         level 0]
    (let [updated-loc (update-score loc level)
          parent-loc (z/up updated-loc)]
      (if (nil? parent-loc)
        updated-loc
        (recur parent-loc (inc level))))))

(defn- ^{:testable true} verify! [loc]
  (-> (if (u/verified? (z/node loc))
        loc
        (let [updatedLoc (z/update loc u/verify!)
              parentLoc (z/up updatedLoc)]
          (if (nil? parentLoc)
            updatedLoc
            (update-parents-scores parentLoc))))
      (z/root)
      (z/node)))

(defn- ^{:testable true} add-user
  [tree inviterId inviteeId]
  (cond (t/empty? tree)
        (-> tree
            (t/replaceRoot (u/create-user inviterId))
            (t/add inviterId)
            (recur inviterId inviteeId))

        ((complement t/containsUid?) tree inviterId)
        tree

        :else
        (let [inviterLoc (t/findFirst tree inviterId)
              updatedInviterLoc (if (t/containsUid? tree inviteeId)
                                  inviterLoc
                                  (z/append-child inviterLoc (u/create-user inviteeId)))
                verifiedInviter (verify! updatedInviterLoc)]
            (-> (t/replaceRoot tree verifiedInviter)
                (t/add inviteeId)))))

(defn- ^{:testable true} scores-map [zipper]
  (reduce (fn [result node]
            (merge result {(:id node) (:score node)}))
          {}
          (ut/users zipper)))

(defn- ^{:testable true} by-score-id
  [x y]
  ;; by score (DESC) -> by id (ASC)
  (compare/cc-cmp [(:score y) (:id x)]
                  [(:score x) (:id y)]))

(defn create-tree []
  (t/create-tree))

(defn get-user [tree userId]
  (if (t/containsUid? tree userId)
    (-> (t/findFirst tree userId)
        (z/node))
    nil))

(defn invite [tree inviter invitee]
  (if (nil? tree)
    (recur (t/create-tree) inviter invitee)
    (add-user tree inviter invitee)))

(defn ranking [tree]
  (if (or (nil? tree) (nil? (:root tree)))
    {}
    (-> (t/nodes tree [:id :score])
        ((partial sort by-score-id)))))