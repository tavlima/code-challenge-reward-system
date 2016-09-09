(ns com.github.tavlima.nubank.reward.invitations.t-domain
  (:use clojure.test
        midje.sweet)
  (:require [clojure.zip :as zip]
            [com.github.tavlima.nubank.reward.invitations.domain :as domain]))

(facts "about `create-user`"
       (fact "it creates a new UNCONFIRMED user, with ZERO score and NO invitations"
             (domain/create-user 1) => {:id 1 :score 0 :invited [] :verified false}
             (domain/create-user 2) => {:id 2 :score 0 :invited [] :verified false}))

(facts "about `add-score`"
       (fact "it returns an updated map with :score added by `score-inc`"
             (domain/add-score {:score 0} 1) => {:score 1}
             (domain/add-score {:score 1} 1/10) => {:score 11/10}
             (domain/add-score {:score 3.5} 1.1) => {:score 4.6}
             (domain/add-score {:score 2} -0.9) => {:score 1.1})

       (fact "it preserves all the unrelated fields"
             (domain/add-score {:score 0 :other [1 2 3]} 1)
             => {:score 1 :other [1 2 3]}))

(facts "about `create-tree`"
       (fact "no args results in an empty tree"
             (domain/create-tree)
             => {:root nil :users #{}})

       (fact "it can be built with pre-defined root and users"
             (domain/create-tree {:id 1 :score 0 :invited [] :verified false} #{1})
             => {:root {:id 1 :score 0 :invited [] :verified false} :users #{1}}))

(facts "about `create-tree-with-user`"
       (fact "it creates a new tree with the a new user as root"
             (domain/create-tree-with-user 1)
             => {:root {:id 1 :score 0 :invited [] :verified false} :users #{1}}

             (domain/create-tree-with-user 2)
             => {:root {:id 2 :score 0 :invited [] :verified false} :users #{2}}))

(facts "about `has-user?`"
       (fact "it checks if a userId exists in the :users hash-set"
             (domain/has-user? {:users #{1 2 "a" :k}} 1) => true
             (domain/has-user? {:users #{1 2 "a" :k}} 2) => true
             (domain/has-user? {:users #{1 2 "a" :k}} "a") => true
             (domain/has-user? {:users #{1 2 "a" :k}} :k) => true
             (domain/has-user? {:users #{1 2 "a" :k}} 3) => false))

(facts "about `make-node`"
       (fact "it returns a new node with all the same attributes, but replaces the :invited value"
             (domain/make-node {:a 1 :b 2} [])
             => {:a 1 :b 2 :invited []}

             (domain/make-node {:a 1 :b 2} [1])
             => {:a 1 :b 2 :invited [1]}

             (domain/make-node {:a 1 :b 2 :invited [:some-values]} [1 2 3])
             => {:a 1 :b 2 :invited [1 2 3]}))

(facts "about `find-first-by-id`"
       (let [root {:id 1 :invited [{:invited [{:id 1 :invited []}]}
                                   {:id 3 :invited [{:id 4 :invited []}]}]}
             zipper (domain/zipper root)]
         (fact "it returns the first loc in the zipper, depth first, which node has the expected :id value"
               (-> (domain/find-first-by-id zipper 4)
                   (zip/node))
               => #(= {:id 4 :invited []} %)

               (-> (domain/find-first-by-id zipper 3)
                   (zip/node))
               => #(= {:id 3 :invited [{:id 4 :invited []}]} %)

               (-> (domain/find-first-by-id zipper 1)
                   (zip/node))
               => #(= root %))

         (fact "it returns nil if no node is found"
               (-> (domain/find-first-by-id zipper 2)
                   (zip/node))
               => nil)))

(facts "about `nodes`"
       (fact "it returns a list of all the nodes in the zipper, depth first, excluding all keys but :id, :score and :verified"
             (-> {:id 1 :score 1 :verified true :dontcare true}
                 (domain/zipper)
                 (domain/nodes))
             => [{:id 1 :score 1 :verified true}]

             (-> {:id 1 :score 1 :verified true :invited [{:id 2 :score 0 :verified true :invited [{:id 5 :score 0 :invited []}]}
                                                          {:id 3 :score 1 :invited [{:id 4 :invited [{}]}]}]}
                 (domain/zipper)
                 (domain/nodes))
             => [{:id 1 :score 1 :verified true}
                 {:id 2 :score 0 :verified true}
                 {:id 5 :score 0}
                 {:id 3 :score 1}
                 {:id 4}
                 {}]))
