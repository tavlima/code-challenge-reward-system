(ns com.github.tavlima.nubank.reward.tree.domain.t-tree
  (use midje.sweet)
  (:require [com.github.tavlima.nubank.reward.tree.domain
             [tree :as t]
             [treenode :as n]
             [location :as l]
             [t-zippable :as tz]
             [t-treenode :as tn]]))

(facts "about `create-tree`"
       (fact "it creates a new tree with no :root and an empty set of :uids"
             (t/create-tree) => {:root nil :uids #{}}))

(facts "about `add`"
       (fact "it adds the uid to the :uids hashset"
             (t/add (t/create-tree) 1) => {:root nil :uids #{1}}
             (t/add (t/create-tree) :anything) => {:root nil :uids #{:anything}}
             (t/add (assoc (t/create-tree) :uids #{:someid}) :otherid) => {:root nil :uids #{:someid :otherid}}))

(facts "about `replaceRoot`"
       (fact "it replaces the Tree :root by the supplied root"
             (t/replaceRoot (t/create-tree) :anything) => {:root :anything :uids #{}}))

(facts "about `containsUid?`"
       (fact "it returns true if the :uids hashmap contains the supplied uid"
             (t/containsUid? (assoc (t/create-tree) :uids #{:anything}) :anything) => true
             (t/containsUid? (t/create-tree) :missing) => false))

(def treeUnique (-> (t/create-tree)
                    (t/replaceRoot {:v 1 :c [{:v 2 :c []}
                                             {:v 3 :s 10 :c [{:v 4 :c [{:v 5 :c []}
                                                                       {:v 6 :c []}]}]}]})))

(def treeRepeated (-> (t/create-tree)
                      (t/replaceRoot {:v 1 :c [{:v :repeated :c []}
                                               {:v 2 :c [{:v 3 :c [{:v :repeated :c []}
                                                                   {:v 4 :c []}]}]}]})))

(facts "about `findFirstByMatcher"
       (fact "it returns the ILocation of the first element, depth-first, for which the supplied matcher returns true"
             (-> (t/findFirstByMatcher treeUnique #(= 5 (n/uid %)))
                 (l/node)
                 (n/uid)) => 5
             (-> (t/findFirstByMatcher treeUnique #(= 1 (n/uid %)))
                 (l/node)
                 (n/uid)) => 1
             (-> (t/findFirstByMatcher treeUnique #(= 10 (:s %)))
                 (l/node)
                 (n/uid)) => 3
             (-> (t/findFirstByMatcher treeRepeated #(= :repeated (n/uid %)))
                 (l/node)) => {:v :repeated :c []})
       (fact "it returns nil if no node is found"
             (t/findFirstByMatcher treeUnique #(= :missing (n/uid %))) => nil))

(facts "about `findFirst`"
       (fact "it returns the ILocation of the first element, depth-first, for which the ITreeNode/matcher function returns true"
             (-> (t/findFirst treeUnique 5)
                 (l/node)
                 (n/uid)) => 5
             (-> (t/findFirst treeUnique 1)
                 (l/node)
                 (n/uid)) => 1
             (-> (t/findFirst treeRepeated :repeated)
                 (l/node)) => {:v :repeated :c []})
       (fact "it returns nil if no node is found"
             (t/findFirst treeUnique :missing) => nil))

(facts "about `nodes`"
       (fact "it iterates over the tree, returning only the requested fields about each nodes (depth-first)"
             (t/nodes treeUnique [:v]) => [{:v 1} {:v 2} {:v 3} {:v 4} {:v 5} {:v 6}]
             (t/nodes treeRepeated [:v]) => [{:v 1} {:v :repeated} {:v 2} {:v 3} {:v :repeated} {:v 4}]
             (t/nodes treeUnique [:v :s]) => [{:v 1} {:v 2} {:v 3 :s 10} {:v 4} {:v 5} {:v 6}]))