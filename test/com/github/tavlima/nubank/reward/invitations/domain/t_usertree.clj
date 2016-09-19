(ns com.github.tavlima.nubank.reward.invitations.domain.t-usertree
  (:use clojure.test
        midje.sweet)
  (:require [com.github.tavlima.nubank.reward.tree.domain.tree :as t]
            [com.github.tavlima.nubank.reward.invitations.domain
             [user :as u]
             [usertree :as ut]]))

(def treeUnique (-> (t/create-tree)
                    (t/replaceRoot (-> (u/create-user 1)
                                       (u/addInvited (u/create-user 2))
                                       (u/addInvited (-> (u/create-user 3)
                                                         (u/addScore 10)
                                                         (u/addInvited (-> (u/create-user 4)
                                                                           (u/addInvited (u/create-user 5))
                                                                           (u/addInvited (u/create-user 6))))))))))

(facts "about `users`"
       (fact "it returns a simplified version of all users, depth-first"
             (ut/users treeUnique) => [{:id 1 :score 0 :verified false}
                                       {:id 2 :score 0 :verified false}
                                       {:id 3 :score 10 :verified false}
                                       {:id 4 :score 0 :verified false}
                                       {:id 5 :score 0 :verified false}
                                       {:id 6 :score 0 :verified false}]))