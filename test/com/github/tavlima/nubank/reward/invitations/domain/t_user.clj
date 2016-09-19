(ns com.github.tavlima.nubank.reward.invitations.domain.t-user
  (:use clojure.test
        midje.sweet)
  (:require [com.github.tavlima.nubank.reward.invitations.domain.user :as u]
            [com.github.tavlima.nubank.reward.tree.domain
             [treenode :as n]
             [zippable :as z]
             [tree :as t]]))

(facts "about `create-user`"
       (fact "it creates a new user with the given id"
             (u/create-user 1) => {:id 1 :invited [] :verified false :score 0}))

(facts "about `match?`"
       (let [userA (u/create-user :a)
             userB (u/create-user :b)
             updatedUserA (assoc userA :score 10)]
         (fact "it compares the :id field, ignoring other fields"
               (n/match? userA :a) => true
               (n/match? userB :a) => false
               (n/match? updatedUserA :a) => true)))

(facts "about `uid`"
       (fact "it returns the :id"
             (n/uid (u/create-user :a)) => :a
             (n/uid (u/create-user :b)) => :b
             (n/uid (u/create-user nil)) => nil))

(facts "about `fields`"
       (fact "it returns the requested keys, ignoring the missing ones"
             (n/fields (u/create-user :a) [:id]) => {:id :a}
             (n/fields (u/create-user :b) [:id :score]) => {:id :b :score 0}
             (n/fields (u/create-user :c) [:id :missing]) => {:id :c}
             (n/fields (u/create-user :c) []) => {}))

(facts "about `children`"
       (fact "it returns the :invited value"
             (z/children (u/create-user :a)) => []
             (z/children (assoc (u/create-user :a) :invited :dontcare)) => :dontcare))

(facts "about `make`"
       (fact "it creates a new User, from the current one, replacing it's children"
             (z/make (u/create-user :a) [:somechildren]) => (assoc (u/create-user :a) :invited [:somechildren])
             (z/make (u/create-user :a) :dontcare) => (assoc (u/create-user :a) :invited :dontcare)))

(facts "about `zipper`"
       (fact "it creates an new Location from the user"
             (-> (u/create-user :a)
                 (z/zipper)
                 (select-keys [:node :path :s-left :s-right]))
             => {:node (u/create-user :a)
                 :path [:nil]
                 :s-left [:nil]
                 :s-right [:nil]}))

(facts "about `addScore`"
       (fact "it sums delta to the current :score"
             (u/addScore (u/create-user :a) 3) => #(= 3 (:score %))
             (u/addScore (u/create-user :b) -1) => #(= -1 (:score %))
             (u/addScore (assoc (u/create-user :c) :score 5) -3) => #(= 2 (:score %))))

(facts "about `verified?`"
       (fact "it returns the :verified value"
             (u/verified? (u/create-user :a)) => false
             (u/verified? (assoc (u/create-user :a) :verified true)) => true))

(facts "about `verify!`"
       (fact "it updates the :verified value to true"
             (u/verify! (u/create-user :a)) => #(= true (:verified %))
             (u/verify! (u/verify! (u/create-user :a))) => #(= true (:verified %))))

(facts "about `addInvited`"
       (fact "it adds the invited user to the :invited vector"
             (let [user1 (u/create-user 1)
                   user2 (u/create-user 2)
                   user3 (u/create-user 3)]
               (u/addInvited user1 user2) => (assoc user1 :invited [user2])
               (u/addInvited (u/addInvited user1 user2) user3) => (assoc user1 :invited [user2 user3]))))
