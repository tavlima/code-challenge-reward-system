(ns com.github.tavlima.nubank.reward.invitations.t-controller
  (:use clojure.test
        midje.sweet
        [midje.util :only [expose-testables]]
        com.github.tavlima.nubank.reward.invitations.controller)
  (:require [clojure.zip :as z]
            [com.github.tavlima.nubank.reward.invitations.domain :as d]))

(expose-testables com.github.tavlima.nubank.reward.invitations.controller)

(facts "about `update-score`"
       (fact "it adds 1/2^level to the loc's node current :score"
             (-> (d/zipper {:score 0})
                 (update-score 0)
                 (z/node))
             => {:score (+ 0 1)}

             (-> (d/zipper {:score 1})
                 (update-score 1)
                 (z/node))
             => {:score (+ 1 1/2)}

             (-> (d/zipper {:score 1/4})
                 (update-score 4)
                 (z/node))
             => {:score (+ 1/4 1/16)})

       (fact "it returns the same loc, unchanged, if level < 0"
             (-> (d/zipper {:score 0})
                 (update-score -1)
                 (z/node))
             => {:score 0}))

(facts "about `update-parents-scores`"
       (fact "it updates the score of the current loc and all it's parents, all the way to the root"
             (-> (d/zipper {:invited [] :score 0})
                 (update-parents-scores)
                 (z/node))
             => {:invited [] :score 1}

             (-> (d/zipper {:invited [{:invited [{:invited [{:invited [] :score 0}] :score 0}] :score 0}] :score 1})
                 (z/down)
                 (update-parents-scores)
                 (z/node))
             => {:invited [{:invited [{:invited [{:invited [] :score 0}] :score 0}] :score 1}] :score 3/2}))

(facts "about `verify!`"
       (fact "it ensures the inviter is marked as verified"
             (-> (d/zipper {:id :inviter :verified false :score 0 :invited [:someone]})
                 (verify!))
             => {:id :inviter :verified true :score 0 :invited [:someone]}

             (-> (d/zipper {:id :inviter :verified true :score 0 :invited [:someone]})
                 (verify!))
             => {:id :inviter :verified true :score 0 :invited [:someone]})

       (fact "if the inviter was not yet verified, it updates scores up to the root, starting at the inviter's parent"
             (-> (d/zipper {:id :parent :verified true :score 0 :invited [{:id :inviter :verified false :score 0 :invited [:someone]}]})
                 (z/down)
                 (verify!))
             => {:id :parent :verified true :score 1 :invited [{:id :inviter :verified true :score 0 :invited [:someone]}]}

             (-> (d/zipper {:id :parent :verified true :score 1 :invited [{:id :inviter :verified true :score 0 :invited [:someone :someoneelse]}]})
                 (z/down)
                 (verify!))
             => {:id :parent :verified true :score 1 :invited [{:id :inviter :verified true :score 0 :invited [:someone :someoneelse]}]}))

(facts "about `add-user`"
       (fact "it ensures the inviter is marked as verified"
             (add-user {:root {:id 1 :verified false :score 0 :invited []} :users #{1}} 1 2)
             => {:root {:id 1 :verified true :score 0 :invited [{:id 2 :verified false :score 0 :invited []}]} :users #{1 2}}

             (add-user {:root {:id :a :verified true :score 0 :invited [{:id "b" :verified false :score 0 :invited []}]} :users #{:a "b"}} :a "b")
             => {:root {:id :a :verified true :score 0 :invited [{:id "b" :verified false :score 0 :invited []}]} :users #{:a "b"}})

       (fact "it adds the new invitee node, but only if it does not yet exist"
             (add-user {:root {:id 1 :verified false :score 0 :invited []} :users #{1}} 1 2)
             => {:root {:id 1 :verified true :score 0 :invited [{:id 2 :verified false :score 0 :invited []}]} :users #{1 2}}

             (add-user {:root {:id 1 :verified false :score 0 :invited []} :users #{1}} 1 1)
             => {:root {:id 1 :verified true :score 0 :invited []} :users #{1}})

       (fact "it locates the inviter node, adds the new invitee node as it's child, adds the invitee id to the tree user's hashset and proceed with the verification process"
             (add-user {:root {:id :a :verified false :score 0 :invited []} :users #{:a}} :a 2)
             => {:root {:id :a :verified true :score 0 :invited [{:id 2 :verified false :score 0 :invited []}]} :users #{:a 2}}

             (add-user {:root {:id 1 :verified true :score 0 :invited [{:id 2 :verified false :score 0 :invited []}]} :users #{1 2}} 2 3)
             => {:root {:id 1 :verified true :score 1 :invited [{:id 2 :verified true :score 0 :invited [{:id 3 :verified false :score 0 :invited []}]}]} :users #{1 2 3}})

       (fact "it updates the scores, even if the invitee already exists"
             (add-user {:root {:id 1 :verified true :score 1 :invited [{:id 2 :verified false :score 0 :invited []}
                                                                       {:id 3 :verified true  :score 0 :invited [{:id 4 :verified false :score 0 :invited []}]}]} :users #{1 2 3 4}} 2 4)
             => {:root {:id 1 :verified true :score 2 :invited [{:id 2 :verified true :score 0 :invited []}
                                                                {:id 3 :verified true :score 0 :invited [{:id 4 :verified false :score 0 :invited []}]}]} :users #{1 2 3 4}}))

(facts "about `scores-map`"
       (fact "it builds a id->score map from the zipper"
             (-> (d/zipper {:id :a :invited [] :score 0})
                 (scores-map))
             => {:a 0}

             (-> (d/zipper {:id 1 :invited [{:id 2 :invited [{:id 3 :invited [{:id 5 :invited [] :score 0}] :score 0}] :score 1} {:id 4 :invited [] :score 0}] :score 3/2})
                 (scores-map))
             => {1 3/2 2 1 3 0 4 0 5 0}))

(facts "about `by-score-id`"
       (fact "it compares the :score (desc) and the :id (asc)"
             (by-score-id {:id 1 :score 0} {:id 2 :score 0}) => #(< % 0)
             (by-score-id {:id :a :score 0} {:id "b" :score 0}) => #(< % 0)
             (by-score-id {:id 1 :score 0} {:id "2" :score 0}) => #(< % 0)
             (by-score-id {:id 2 :score 0} {:id 1 :score 0}) => #(> % 0)
             (by-score-id {:id 1 :score 0} {:id 2 :score 1}) => #(> % 0)
             (by-score-id {:id 2 :score 1} {:id 1 :score 0}) => #(< % 0)))

(facts "about `invite`"
       (fact "it adds the invited user to the tree"
             (invite {:root {:id 1 :verified false :score 0 :invited []} :users #{1}} 1 "2")
             => {:root {:id 1 :verified true :score 0 :invited [{:id "2" :verified false :score 0 :invited []}]} :users #{1 "2"}})

       (fact "it ignores invitations if the inviter does not exist "
             (invite {:root {:id 1 :verified true :score 0 :invited [{:id 2 :verified false :score 0 :invited []}]} :users #{1 2}} 3 4)
             => {:root {:id 1 :verified true :score 0 :invited [{:id 2 :verified false :score 0 :invited []}]} :users #{1 2}})

       (fact "it does not create a new node, if the invitee already exists, but it updates the scores"
             (invite {:root {:id 1 :verified true :score 0 :invited [{:id 2 :verified false :score 0 :invited []} {:id 3 :verified false :score 0 :invited []}]} :users #{1 2 3}} 2 3)
             => {:root {:id 1 :verified true :score 1 :invited [{:id 2 :verified true :score 0 :invited []} {:id 3 :verified false :score 0 :invited []}]} :users #{1 2 3}})

       (fact "it populates an empty tree with the inviter user, if the tree is empty"
             (invite nil 1 2)
             => {:root {:id 1 :verified true :score 0 :invited [{:id 2 :verified false :score 0 :invited []}]} :users #{1 2}}

             (invite {:root nil :users #{}} 1 2)
             => {:root {:id 1 :verified true :score 0 :invited [{:id 2 :verified false :score 0 :invited []}]} :users #{1 2}}))

(facts "about `empty-tree`"
       (fact "it creates an empty tree"
             (create-tree) => {:root nil :users #{}}))

(facts "about `ranking`"
       (fact "it builds a score ranking sorted map"
             (ranking {:root {:id 1 :score 0}})
             => [{:id 1 :score 0}]

             (ranking {:root {:id 1 :invited [{:id 2 :invited [{:id 3 :invited [{:id 5 :invited [] :score 0}] :score 0}] :score 1} {:id 4 :invited [] :score 0}] :score 3/2}})
             => [{:id 1 :score 3/2} {:id 2 :score 1} {:id 3 :score 0} {:id 4 :score 0} {:id 5 :score 0}]

             (ranking {:root {:id 1 :invited [{:id 2 :invited [{:id 3 :invited [{:id 5 :invited [] :score 10/3}] :score 0}] :score 1} {:id 4 :invited [] :score 0}] :score 3/2}})
             => [{:id 5 :score 10/3} {:id 1 :score 3/2} {:id 2 :score 1} {:id 3 :score 0} {:id 4 :score 0}])

       (fact "it returns an empty map if the tree is nil or it's :root is nil"
             (ranking nil) => {}
             (ranking {:root nil}) => {}
             (ranking {:no-root :something}) => {}))

(facts "about `get-user`"
       (fact "it returns the user node, if it exists"
             (get-user {:root {:id 1 :score 0} :users #{1}} 1)
             => {:id 1 :score 0}

             (get-user {:root {:id 1 :invited [{:id 2 :invited [{:id 3 :invited [{:id 5 :invited [] :score 0}] :score 0}] :score 1} {:id 4 :invited [] :score 0}] :score 3/2} :users #{1 2 3 4 5}} 3)
             => {:id 3 :invited [{:id 5 :invited [] :score 0}] :score 0})

       (fact "it returns nil if the user does not exist (fast-fails by checking :users hashset)"
             (get-user {:root {:id 1 :score 0} :users #{1}} 2)
             => nil))
