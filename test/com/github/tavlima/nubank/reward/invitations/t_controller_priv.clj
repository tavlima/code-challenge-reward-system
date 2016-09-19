(ns com.github.tavlima.nubank.reward.invitations.t-controller-priv
  (:use clojure.test
        midje.sweet
        [midje.util :only [expose-testables]]
        com.github.tavlima.nubank.reward.invitations.controller)
  (:require [com.github.tavlima.nubank.reward.tree.domain
             [location :as l]
             [zippable :as z]
             [tree :as t]]
            [com.github.tavlima.nubank.reward.invitations.domain
             [user :as u]]))

(expose-testables com.github.tavlima.nubank.reward.invitations.controller)

(defn- test-user
  ([] (u/create-user :default))
  ([ks] (merge (u/create-user :default) ks)))

(defn- new-tree [root uids]
  (merge (t/create-tree) {:root root :uids uids}))

(facts "about `update-score`"
       (fact "it adds 1/2^level to the loc's node current :score"
             (-> (z/zipper (test-user {:score 0}))
                 (update-score 0)
                 (l/node))
             => (test-user {:score (+ 0 1)})

             (-> (z/zipper (test-user {:score 1}))
                 (update-score 1)
                 (l/node))
             => (test-user {:score (+ 1 1/2)})

             (-> (z/zipper (test-user {:score 1/4}))
                 (update-score 4)
                 (l/node))
             => (test-user {:score (+ 1/4 1/16)}))

       (fact "it returns the same loc, unchanged, if level < 0"
             (-> (z/zipper (test-user {:score 0}))
                 (update-score -1)
                 (l/node))
             => (test-user {:score 0})))

(facts "about `update-parents-scores`"
       (fact "it updates the score of the current loc and all it's parents, all the way to the root"
             (let [user4 (test-user {:score 0})
                   user3 (test-user {:score 0 :invited [user4]})
                   user2 (test-user {:score 0 :invited [user3]})
                   user1 (test-user {:score 1 :invited [user2]})]
               (-> (z/zipper user4)
                   (update-parents-scores)
                   (l/node))
               => (assoc user4 :score 1)

               (-> (z/zipper user1)
                   (l/down)
                   (update-parents-scores)
                   (l/node))
               => (assoc user1 :score 3/2
                               :invited [(assoc user2 :score 1)]))))

(facts "about `verify!`"
       (let [not-verified-user (test-user)
             verified-user (test-user {:verified true})
             not-verified-with-parent (test-user {:id :parent :score 0 :verified true :invited [(test-user {:id :inviter})]})
             verified-with-parent (test-user {:id :parent :score 0 :verified true :invited [(test-user {:id :inviter :verified true})]})]
         (fact "it ensures the inviter is marked as verified"
               (-> (z/zipper not-verified-user)
                   (verify!))
               => verified-user

               (-> (z/zipper verified-user)
                   (verify!))
               => verified-user)

         (fact "if the inviter was not yet verified, it updates scores up to the root, starting at the inviter's parent"
               (-> (z/zipper not-verified-with-parent)
                   (l/down)
                   (verify!))
               => (-> not-verified-with-parent
                      (assoc :score 1)
                      (assoc-in [:invited 0 :verified] true))

               (-> (z/zipper verified-with-parent)
                   (l/down)
                   (verify!))
               => (-> verified-with-parent
                      (assoc-in [:invited 0 :verified] true)))))

(facts "about `add-user`"
       (let [tree (new-tree (-> (u/create-user 1)
                                (u/verify!)
                                (u/addInvited (-> (u/create-user 2)
                                                  (u/verify!)
                                                  (u/addInvited (u/create-user 3)))))
                            #{1 2 3})]

         (fact "it locates the inviter node, adds the new invitee node as it's child, adds the invitee id to the tree user's hashset and proceed with the verification process"
               (add-user tree 1 3) => tree

               (add-user tree 2 4) => (-> tree
                                          (assoc-in [:root :invited 0 :invited 1] (u/create-user 4)) ; adds (4) to (2)
                                          (update :uids #(conj % 4))) ; adds 4 to the tree hashset

               (add-user tree 3 2) => (-> tree
                                          (assoc-in [:root :invited 0 :invited 0 :verified] true) ; marks (3) as verified
                                          (update-in [:root :invited 0 :score] #(+ % 1)) ; adds +1 to (2) score
                                          (update-in [:root :score] #(+ % 1/2))) ; adds +1/2 to (1) score

               (add-user tree 3 4) => (-> tree
                                          (assoc-in [:root :invited 0 :invited 0 :verified] true) ; marks (3) as verified
                                          (assoc-in [:root :invited 0 :invited 0 :invited] [(u/create-user 4)]) ; adds (4) to (3)
                                          (update-in [:root :invited 0 :score] #(+ % 1)) ; adds +1 to (2) score
                                          (update-in [:root :score] #(+ % 1/2)) ; adds +1/2 to (1) score
                                          (update :uids #(conj % 4)))) ; adds 4 to the tree hashset

         (fact "it works even on empty trees"
               (add-user (t/create-tree) 1 2) => (-> (t/create-tree)
                                                     (assoc :root (-> (u/create-user 1)
                                                                      (u/verify!)
                                                                      (u/addInvited (u/create-user 2))))
                                                     (assoc :uids #{1 2})))

         (fact "it ignores the addition if the inviter doesn't exist"
                 (add-user tree :inviter :invitee) => tree)))

(facts "about `scores-map`"
       (fact "it builds a id->score map from the zipper"
             (-> (t/create-tree)
                 (t/replaceRoot (u/create-user :a))
                 (scores-map))
             => {:a 0}

             (-> (t/create-tree)
                 (add-user 1 2)
                 (add-user 1 3)
                 (add-user 3 4)
                 (add-user 2 4)
                 (add-user 4 5)
                 (add-user 4 6)
                 (scores-map))
             => {1 5/2 2 0 3 1 4 0 5 0 6 0}))

(facts "about `by-score-id`"
       (fact "it compares the :score (desc) and the :id (asc)"
             (by-score-id {:id 1 :score 0} {:id 2 :score 0}) => #(< % 0)
             (by-score-id {:id :a :score 0} {:id "b" :score 0}) => #(< % 0)
             (by-score-id {:id 1 :score 0} {:id "2" :score 0}) => #(< % 0)
             (by-score-id {:id 2 :score 0} {:id 1 :score 0}) => #(> % 0)
             (by-score-id {:id 1 :score 0} {:id 2 :score 1}) => #(> % 0)
             (by-score-id {:id 2 :score 1} {:id 1 :score 0}) => #(< % 0)))