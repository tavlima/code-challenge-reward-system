(ns com.github.tavlima.nubank.reward.invitations.controller
  (require [clojure.zip :as z]
           [com.github.tavlima.nubank.reward
            [util :as util]
            [compare :as compare]]
           [com.github.tavlima.nubank.reward.invitations.domain :as domain]))

(defn- ^{:testable true} update-score [loc level]
  (if (>= level 0)
    (z/edit loc domain/add-score (util/exp 1/2 level))
    loc))

(defn- ^{:testable true} update-parents-scores [zipper]
  (loop [loc zipper
         level 0]
    (let [updated-loc (update-score loc level)
          parent-loc (z/up updated-loc)]
      (if (nil? parent-loc)
        updated-loc
        (recur parent-loc (inc level))))))

(defn- ^{:testable true} verify! [inviter]
  (z/root (if (domain/verified? (z/node inviter))
            inviter
            (let [updatedInviter (z/edit inviter #(merge % {:verified true}))
                  parent (z/up updatedInviter)]
              (if (nil? parent)
                updatedInviter
                (update-parents-scores parent))))))

(defn- ^{:testable true} add-user
  [tree inviterId inviteeId]
  (let [inviter (-> (domain/zipper (:root tree))
                    (domain/find-first-by-id inviterId))
        updatedParent (if (domain/has-user? tree inviteeId)
                        inviter
                        (z/append-child inviter (domain/create-user inviteeId)))]
    (-> updatedParent
        (verify!)
        (domain/create-tree (conj (:users tree) inviteeId)))))

(defn- ^{:testable true} scores-map [zipper]
  (reduce (fn [result node]
            (merge result {(:id node) (:score node)}))
          {}
          (domain/nodes zipper)))

(defn- ^{:testable true} by-score-id
  [x y]
  ;; by score (DESC) -> by id (ASC)
  (compare/cc-cmp [(:score y) (:id x)]
               [(:score x) (:id y)]))

(defn create-tree []
  (domain/create-tree))

(defn get-user [tree userId]
  (if (domain/has-user? tree userId)
    (-> (domain/find-first-by-id (domain/zipper (:root tree)) userId)
        (z/node))
    nil))

(defn invite [tree inviter invitee]
  (let [populated-tree (if (or (nil? tree)
                               (nil? (:root tree)))
                         (domain/create-tree-with-user inviter)
                         tree)]
    (if ((complement domain/has-user?) populated-tree inviter)
      populated-tree
      (add-user populated-tree inviter invitee))))

(defn ranking [tree]
  (if (or (nil? tree) (nil? (:root tree)))
    {}
    (-> (:root tree)
        (domain/zipper)
        (domain/nodes [:id :score])
        ((partial sort by-score-id)))))