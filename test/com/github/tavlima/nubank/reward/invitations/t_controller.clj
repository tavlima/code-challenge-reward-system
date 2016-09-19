(ns com.github.tavlima.nubank.reward.invitations.t-controller
  (:use clojure.test
        midje.sweet
        [midje.util :only [expose-testables]]
        com.github.tavlima.nubank.reward.invitations.controller)
  (:require [com.github.tavlima.nubank.reward.tree.domain.tree :as t]
            [com.github.tavlima.nubank.reward.invitations.domain.user :as u]))

(expose-testables com.github.tavlima.nubank.reward.invitations.controller)

(defn- new-tree [root uids]
  (merge (t/create-tree) {:root root :uids uids}))

(facts "about `create-tree`"
       (fact "it creates an empty tree"
             (create-tree) => (t/create-tree)))

(facts "about `get-user`"
       (let [user3 (u/create-user 3)
             user2 (-> (u/create-user 2)
                       (u/addInvited user3))
             user1 (-> (u/create-user 1)
                       (u/addInvited user2))
             tree (new-tree user1 #{1 2 3})]

         (fact "it returns the user node, if it exists"
               (get-user tree 1) => user1
               (get-user tree 3) => user3
               (get-user tree :missing) => nil)))

(facts "about `invite`"
       (let [emptyTree (t/create-tree)
             tree12 (add-user emptyTree 1 2)]

         (fact "it adds the invited user to the tree"
               (invite tree12 2 3) => (add-user tree12 2 3)
               (invite tree12 1 3) => (add-user tree12 1 3))

         (fact "it ignores invitations if the inviter does not exist "
               (invite tree12 :missing :new) => tree12)

         (fact "it does not create a new node, if the invitee already exists, but it updates the scores"
               (invite tree12 2 1) => (-> tree12
                                          (assoc-in [:root :score] 1)
                                          (assoc-in [:root :invited 0 :verified] true)))

         (fact "it populates an empty tree with the inviter user, if the tree is empty"
               (invite nil 1 2) => tree12
               (invite (t/create-tree) 1 2) => tree12)))

(facts "about `ranking`"
       (let [tree (-> (t/create-tree)
                      (add-user 1 2)
                      (add-user 1 3)
                      (add-user 3 4)
                      (add-user 2 4)
                      (add-user 4 5)
                      (add-user 4 6))]

         (fact "it builds a score ranking sorted map"
               (ranking tree) => [{:id 1 :score 5/2}
                                  {:id 3 :score 1}
                                  {:id 2 :score 0}
                                  {:id 4 :score 0}
                                  {:id 5 :score 0}
                                  {:id 6 :score 0}]

               (ranking (add-user tree 4 7)) => [{:id 1 :score 5/2}
                                                 {:id 3 :score 1}
                                                 {:id 2 :score 0}
                                                 {:id 4 :score 0}
                                                 {:id 5 :score 0}
                                                 {:id 6 :score 0}
                                                 {:id 7 :score 0}]

               (ranking (add-user tree 6 5)) => [{:id 1 :score (+ 5/2 1/4)}
                                                 {:id 3 :score (+ 1 1/2)}
                                                 {:id 4 :score (+ 0 1)}
                                                 {:id 2 :score 0}
                                                 {:id 5 :score 0}
                                                 {:id 6 :score 0}])

         (fact "it returns an empty map if the tree is nil or it's :root is nil"
               (ranking nil) => {}
               (ranking (t/create-tree)) => {})))
