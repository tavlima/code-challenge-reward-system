(ns com.github.tavlima.nubank.reward.tree.t-location
  (require [com.github.tavlima.nubank.reward.tree.location :as z])
  (use midje.sweet)
  (:import (clojure.lang Keyword PersistentArrayMap)))

(extend-protocol z/ILocatable
  PersistentArrayMap
  (children [map]
    (:children map))
  (make [map children]
    (assoc map :children children))
  (zipper [map]
    (z/->Location map [:nil] [:nil] [:nil])))

(extend-protocol z/ILocatable
  Keyword
  (children [_] (list))
  (make [key _] key)
  (zipper [key]
    (z/->Location key [:nil] [:nil] [:nil])))

(def nodeG {:value :g :children []})
(def nodeE {:value :e :children []})
(def nodeF {:value :f :children []})
(def nodeB {:value :b :children [nodeE, nodeF]})
(def nodeC {:value :c :children [nodeG]})
(def nodeD {:value :d :children []})
(def nodeA {:value :a :children [nodeB, nodeC, nodeD]})
(def lonelyRoot {:value :lonely :children []})

(facts "about `node`"
       (fact "it returns the :node at any location"
             (z/node (z/zipper {:value "my node"}))
             => {:value "my node"}

             (z/node (z/zipper {:value "my node 2"}))
             => {:value "my node 2"}))

(facts "about `end?`"
       (fact "it returns true if :node is :nil and false, otherwise"
             (z/end? (z/zipper :nil))
             => true

             (z/end? (z/zipper :whatever))
             => false))

(facts "about `replace`"
       (fact "it replaces the node at current location by the one passed as argument"
             (z/replace (z/zipper {:value 1}) {:anothervalue 2})
             => {:node  {:anothervalue 2}
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}))

(facts "about `update`"
       (fact "it calls the function, passing the current node, and updates only the :node value"
             (z/update (z/zipper {:value 1}) #(assoc % :value (inc (:value %))))
             => {:node  {:value 2}
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}))

(facts "about `append-child`"
       (fact "it returns an updated location with the new child"
             (-> (z/zipper nodeD)
                 (z/append-child nodeE))
             => {:node  {:value :d :children [{:value :e :children []}]}
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}
             (-> (z/zipper nodeC)
                 (z/append-child nodeD))
             => {:node  {:value :c :children [{:value :g :children []}
                                              {:value :d :children []}]}
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}))

(facts "about `down`"
       (fact "it returns nil if the node has no child"
             (-> (z/zipper nodeG)
                 (z/down))
             => nil)
       (fact "it moves to nodeB"
             (-> (z/zipper nodeA)
                 (z/down))
             => {:node  nodeB
                 :path  [nodeA :nil]
                 :s-left  [[:nil]]
                 :s-right [nodeC nodeD [:nil]]})
       (fact "it moves to nodeE"
             (-> (z/zipper nodeA)
                 (z/down)
                 (z/down))
             => {:node  nodeE
                 :path  [nodeB nodeA :nil]
                 :s-left  [[[:nil]]]
                 :s-right [nodeF [nodeC nodeD [:nil]]]}))

(facts "about `right`"
       (fact "it returns nil if the node has no right siblings"
             (-> (z/zipper nodeC)
                 (z/down)
                 (z/right))
             => nil)
       (fact "it moves to nodeC"
             (-> (z/zipper nodeA)
                 (z/down)
                 (z/right))
             => {:node  nodeC
                 :path  [nodeA :nil]
                 :s-left  [[:nil] nodeB]
                 :s-right [nodeD [:nil]]})
       (fact "it moves to nodeF"
             (-> (z/zipper nodeA)
                 (z/down)
                 (z/down)
                 (z/right))
             => {:node  nodeF
                 :path  [nodeB nodeA :nil]
                 :s-left  [[[:nil]] nodeE]
                 :s-right [[nodeC nodeD [:nil]]]}))

(facts "about `up`"
       (fact "it returns nil if already at the root"
             (z/up (z/zipper lonelyRoot)) => nil)
       (fact "it moves from B to A"
             (-> (z/zipper nodeA) (z/next) (z/up))
             => (z/zipper nodeA))
       (fact "it moves from F to B"
             (-> (z/zipper nodeA) (z/next) (z/next) (z/next) (z/up))
             => {:node  nodeB
                 :path  [nodeA :nil]
                 :s-left  [[:nil]]
                 :s-right [nodeC nodeD [:nil]]})
       (fact "it preserves the updates"
             (-> (z/zipper nodeC) (z/next) (z/update #(assoc % :updated true)) (z/up))
             => {:node  {:value :c :children [(assoc nodeG :updated true)]}
                 :s-left  [:nil]
                 :path  [:nil]
                 :s-right [:nil]}))

(facts "about `root`"
       (fact "it moves up to the root"
             (-> nodeA
                 (z/zipper)
                 (z/next) (z/next) (z/next) (z/next) (z/next) (z/next)
                 (z/root))
             => {:node  nodeA
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}
             (-> nodeA
                 (z/zipper)
                 (z/root))
             => {:node  nodeA
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}))

(facts "about `next`"
       (fact "end? is true after lonelyNode"
             (z/end? (z/next (z/zipper lonelyRoot)))
             => true)
       (fact "it moves to nodeB"
             (-> nodeA
                 (z/zipper)
                 (z/next))
             => {:node  nodeB
                 :path  [nodeA :nil]
                 :s-left  [[:nil]]
                 :s-right [nodeC nodeD [:nil]]})
       (fact "it moves to nodeE"
             (-> nodeA
                 (z/zipper)
                 (z/next) (z/next))
             => {:node  nodeE
                 :path  [nodeB nodeA :nil]
                 :s-left  [[[:nil]]]
                 :s-right [nodeF [nodeC nodeD [:nil]]]})
       (fact "it moves to nodeF"
             (-> nodeA
                 (z/zipper)
                 (z/next) (z/next) (z/next))
             => {:node  nodeF
                 :path  [nodeB nodeA :nil]
                 :s-left  [[[:nil]] nodeE]
                 :s-right [[nodeC nodeD [:nil]]]})
       (fact "it moves to nodeC"
             (-> nodeA
                 (z/zipper)
                 (z/next) (z/next) (z/next) (z/next))
             => {:node  nodeC
                 :path  [nodeA :nil]
                 :s-left  [[:nil] nodeB]
                 :s-right [nodeD [:nil]]})
       (fact "it moves to nodeG"
             (-> nodeA
                 (z/zipper)
                 (z/next) (z/next) (z/next) (z/next) (z/next))
             => {:node  nodeG
                 :path  [nodeC nodeA :nil]
                 :s-left  [[[:nil] nodeB]]
                 :s-right [[nodeD [:nil]]]})
       (fact "it moves to nodeD"
             (-> nodeA
                 (z/zipper)
                 (z/next) (z/next) (z/next) (z/next) (z/next) (z/next))
             => {:node  nodeD
                 :path  [nodeA :nil]
                 :s-left  [[:nil] nodeB nodeC]
                 :s-right [[:nil]]}))