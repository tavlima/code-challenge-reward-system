(ns com.github.tavlima.nubank.reward.tree.t-domain
  (require [com.github.tavlima.nubank.reward.tree
            [domain :as d]
            [location :as l]]
           [com.github.tavlima.nubank.reward.tree.location :as loc])
  (use midje.sweet))

;; User record
(facts "about `create-user`"
       (fact "it creates a new user with the given id"
             (d/create-user 1) => {:id 1 :invited [] :verified false :score 0}))

(facts "about `match?`"
       (let [userA (d/create-user :a)
             userB (d/create-user :b)
             updatedUserA (assoc userA :score 10)]
         (fact "it compares the :id field, ignoring other fields"
               (d/match? userA :a) => true
               (d/match? userB :a) => false
               (d/match? updatedUserA :a) => true)))

(facts "about `uid`"
       (fact "it returns the :id"
             (d/uid (d/create-user :a)) => :a
             (d/uid (d/create-user :b)) => :b
             (d/uid (d/create-user nil)) => nil))

(facts "about `fields`"
       (fact "it returns the requested keys, ignoring the missing ones"
             (d/fields (d/create-user :a) [:id]) => {:id :a}
             (d/fields (d/create-user :b) [:id :score]) => {:id :b :score 0}
             (d/fields (d/create-user :c) [:id :missing]) => {:id :c}
             (d/fields (d/create-user :c) []) => {}))

(facts "about `children`"
       (fact "it returns the :invited value"
             (l/children (d/create-user :a)) => []
             (l/children (assoc (d/create-user :a) :invited :dontcare)) => :dontcare))

(facts "about `make`"
       (fact "it creates a new User, from the current one, replacing it's children"
             (l/make (d/create-user :a) [:somechildren]) => (assoc (d/create-user :a) :invited [:somechildren])
             (l/make (d/create-user :a) :dontcare) => (assoc (d/create-user :a) :invited :dontcare)))

(facts "about `zipper`"
       (fact "it creates an new Location from the user"
             (-> (d/create-user :a)
                 (l/zipper)
                 (select-keys [:node :path :s-left :s-right]))
             => {:node (d/create-user :a)
                 :path [:nil]
                 :s-left [:nil]
                 :s-right [:nil]}))

(facts "about `addScore`"
       (fact "it sums delta to the current :score"
             (d/addScore (d/create-user :a) 3) => #(= 3 (:score %))
             (d/addScore (d/create-user :b) -1) => #(= -1 (:score %))
             (d/addScore (assoc (d/create-user :c) :score 5) -3) => #(= 2 (:score %))))

(facts "about `verified?`"
       (fact "it returns the :verified value"
             (d/verified? (d/create-user :a)) => false
             (d/verified? (assoc (d/create-user :a) :verified true)) => true))

(facts "about `verify!`"
       (fact "it updates the :verified value to true"
             (d/verify! (d/create-user :a)) => #(= true (:verified %))
             (d/verify! (d/verify! (d/create-user :a))) => #(= true (:verified %))))

(facts "about `addInvited`"
       (fact "it adds the invited user to the :invited vector"
             (let [user1 (d/create-user 1)
                   user2 (d/create-user 2)
                   user3 (d/create-user 3)]
               (d/addInvited user1 user2) => (assoc user1 :invited [user2])
               (d/addInvited (d/addInvited user1 user2) user3) => (assoc user1 :invited [user2 user3]))))

;; Tree record
(facts "about `create-tree`"
       (fact "it creates a new tree with no :root and an empty set of :uids"
             (d/create-tree) => {:root nil :uids #{}}))

(facts "about `add`"
       (fact "it adds the uid to the :uids hashset"
             (d/add (d/create-tree) 1) => {:root nil :uids #{1}}
             (d/add (d/create-tree) :anything) => {:root nil :uids #{:anything}}
             (d/add (assoc (d/create-tree) :uids #{:someid}) :otherid) => {:root nil :uids #{:someid :otherid}}))

(facts "about `replaceRoot`"
       (fact "it replaces the Tree :root by the supplied root"
             (d/replaceRoot (d/create-tree) :anything) => {:root :anything :uids #{}}))

(facts "about `containsUid?`"
       (fact "it returns true if the :uids hashmap contains the supplied uid"
             (d/containsUid? (assoc (d/create-tree) :uids #{:anything}) :anything) => true
             (d/containsUid? (d/create-tree) :missing) => false))

(def treeUnique (-> (d/create-tree)
                    (d/replaceRoot (-> (d/create-user 1)
                                       (d/addInvited (d/create-user 2))
                                       (d/addInvited (-> (d/create-user 3)
                                                         (d/addScore 10)
                                                         (d/addInvited (-> (d/create-user 4)
                                                                           (d/addInvited (d/create-user 5))
                                                                           (d/addInvited (d/create-user 6))))))))))

(def treeRepeated (-> (d/create-tree)
                      (d/replaceRoot (-> (d/create-user 1)
                                         (d/addInvited (d/create-user :repeated))
                                         (d/addInvited (-> (d/create-user 2)
                                                           (d/addInvited (-> (d/create-user 3)
                                                                             (d/addInvited (d/create-user :repeated))
                                                                             (d/addInvited (d/create-user 4))))))))))

(facts "about `findFirstByMatcher"
       (fact "it returns the ILocation of the first element, depth-first, for which the supplied matcher returns true"
             (-> (d/findFirstByMatcher treeUnique #(= 5 (d/uid %)))
                 (loc/node)
                 (d/uid)) => 5
             (-> (d/findFirstByMatcher treeUnique #(= 1 (d/uid %)))
                 (loc/node)
                 (d/uid)) => 1
             (-> (d/findFirstByMatcher treeUnique #(= 10 (:score %)))
                 (loc/node)
                 (d/uid)) => 3
             (-> (d/findFirstByMatcher treeRepeated #(= :repeated (d/uid %)))
                 (loc/node)) => {:id :repeated :invited [] :score 0 :verified false})
       (fact "it returns nil if no node is found"
             (d/findFirstByMatcher treeUnique #(= :missing (d/uid %))) => nil))

(facts "about `findFirst`"
       (fact "it returns the ILocation of the first element, depth-first, for which the ITreeNode/matcher function returns true"
             (-> (d/findFirst treeUnique 5)
                 (loc/node)
                 (d/uid)) => 5
             (-> (d/findFirst treeUnique 1)
                 (loc/node)
                 (d/uid)) => 1
             (-> (d/findFirst treeRepeated :repeated)
                 (loc/node)) => {:id :repeated :invited [] :score 0 :verified false})
       (fact "it returns nil if no node is found"
             (d/findFirst treeUnique :missing) => nil))

(facts "about `nodes`"
       (fact "it iterates over the tree, returning only the requested fields about each nodes (depth-first)"
             (d/nodes treeUnique [:id]) => [{:id 1} {:id 2} {:id 3} {:id 4} {:id 5} {:id 6}]
             (d/nodes treeRepeated [:id]) => [{:id 1} {:id :repeated} {:id 2} {:id 3} {:id :repeated} {:id 4}]
             (d/nodes treeUnique [:id :score]) => [{:id 1 :score 0} {:id 2 :score 0} {:id 3 :score 10} {:id 4 :score 0} {:id 5 :score 0} {:id 6 :score 0}])
       (fact "it defaults to :id, :score and :verified fields"
             (d/nodes treeUnique) => [{:id 1 :score 0 :verified false} {:id 2 :score 0 :verified false} {:id 3 :score 10 :verified false} {:id 4 :score 0 :verified false} {:id 5 :score 0 :verified false} {:id 6 :score 0 :verified false}]))