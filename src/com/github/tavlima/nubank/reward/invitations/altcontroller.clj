(ns com.github.tavlima.nubank.reward.invitations.altcontroller
  (require [com.github.tavlima.nubank.reward.tree
            [location :as z]
            [domain :as d]]
           [com.github.tavlima.nubank.reward
            [util :as util]
            [compare :as compare]]))

(defn- ^{:testable true} update-score [loc level]
  (if (>= level 0)
    (z/update loc #(d/addScore % (util/exp 1/2 level)))
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
  (-> (if (d/verified? (z/node loc))
        loc
        (let [updatedLoc (z/update loc d/verify!)
              parentLoc (z/up updatedLoc)]
          (if (nil? parentLoc)
            updatedLoc
            (update-parents-scores parentLoc))))
      (z/root)
      (z/node)))

(defn- ^{:testable true} add-user
  [tree inviterId inviteeId]
  (cond (d/empty? tree)
        (-> tree
            (d/replaceRoot (d/create-user inviterId))
            (d/add inviterId)
            (recur inviterId inviteeId))

        ((complement d/containsUid?) tree inviterId)
        tree

        :else
        (let [inviterLoc (d/findFirst tree inviterId)
              updatedInviterLoc (if (d/containsUid? tree inviteeId)
                                    inviterLoc
                                    (z/append-child inviterLoc (d/create-user inviteeId)))
                verifiedInviter (verify! updatedInviterLoc)]
            (-> (d/replaceRoot tree verifiedInviter)
                (d/add inviteeId)))))

(defn- ^{:testable true} scores-map [zipper]
  (reduce (fn [result node]
            (merge result {(:id node) (:score node)}))
          {}
          (d/nodes zipper)))

(defn- ^{:testable true} by-score-id
  [x y]
  ;; by score (DESC) -> by id (ASC)
  (compare/cc-cmp [(:score y) (:id x)]
                  [(:score x) (:id y)]))

(defn create-tree []
  (d/create-tree))

(defn get-user [tree userId]
  (if (d/containsUid? tree userId)
    (-> (d/findFirst tree userId)
        (z/node))
    nil))

(defn invite [tree inviter invitee]
  (if (nil? tree)
    (recur (d/create-tree) inviter invitee)
    (add-user tree inviter invitee)))

(defn ranking [tree]
  (if (or (nil? tree) (nil? (:root tree)))
    {}
    (-> (d/nodes tree [:id :score])
        ((partial sort by-score-id)))))