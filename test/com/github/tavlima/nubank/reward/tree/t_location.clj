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
                 :left  [:nil]
                 :right [:nil]}))

(facts "about `update`"
       (fact "it calls the function, passing the current node, and updates only the :node value"
             (z/update (z/zipper {:value 1}) #(assoc % :value (inc (:value %))))
             => {:node  {:value 2}
                 :path  [:nil]
                 :left  [:nil]
                 :right [:nil]}))

(facts "about `append-child`"
       (fact "it returns an updated location with the new child"
             (-> (z/zipper nodeD)
                 (z/append-child nodeE))
             => {:node  {:value :d :children [{:value :e :children []}]}
                 :path  [:nil]
                 :left  [:nil]
                 :right [:nil]}
             (-> (z/zipper nodeC)
                 (z/append-child nodeD))
             => {:node  {:value :c :children [{:value :g :children []}
                                              {:value :d :children []}]}
                 :path  [:nil]
                 :left  [:nil]
                 :right [:nil]}))

(facts "about `goDown`"
       (fact "it returns nil if the node has no child"
             (-> (z/zipper nodeG)
                 (z/goDown))
             => nil)
       (fact "it moves to nodeB"
             (-> (z/zipper nodeA)
                 (z/goDown))
             => {:node  nodeB
                 :path  [nodeA :nil]
                 :left  [[:nil]]
                 :right [nodeC nodeD [:nil]]})
       (fact "it moves to nodeE"
             (-> (z/zipper nodeA)
                 (z/goDown)
                 (z/goDown))
             => {:node  nodeE
                 :path  [nodeB nodeA :nil]
                 :left  [[[:nil]]]
                 :right [nodeF [nodeC nodeD [:nil]]]}))

(facts "about `goRight`"
       (fact "it returns nil if the node has no right siblings"
             (-> (z/zipper nodeC)
                 (z/goDown)
                 (z/goRight))
             => nil)
       (fact "it moves to nodeC"
             (-> (z/zipper nodeA)
                 (z/goDown)
                 (z/goRight))
             => {:node  nodeC
                 :path  [nodeA :nil]
                 :left  [[:nil] nodeB]
                 :right [nodeD [:nil]]})
       (fact "it moves to nodeF"
             (-> (z/zipper nodeA)
                 (z/goDown)
                 (z/goDown)
                 (z/goRight))
             => {:node  nodeF
                 :path  [nodeB nodeA :nil]
                 :left  [[[:nil]] nodeE]
                 :right [[nodeC nodeD [:nil]]]}))

(facts "about `goUp`"
       (fact "it returns nil if already at the root"
             (z/goUp (z/zipper lonelyRoot)) => nil)
       (fact "it moves from B to A"
             (-> (z/zipper nodeA) (z/goNext) (z/goUp))
             => (z/zipper nodeA))
       (fact "it moves from F to B"
             (-> (z/zipper nodeA) (z/goNext) (z/goNext) (z/goNext) (z/goUp))
             => {:node  nodeB
                 :path  [nodeA :nil]
                 :left  [[:nil]]
                 :right [nodeC nodeD [:nil]]})
       (fact "it preserves the updates"
             (-> (z/zipper nodeC) (z/goNext) (z/update #(assoc % :updated true)) (z/goUp))
             => {:node  {:value :c :children [(assoc nodeG :updated true)]}
                 :left  [:nil]
                 :path  [:nil]
                 :right [:nil]}))

(facts "about `root`"
       (fact "it moves up to the root"
             (-> nodeA
                 (z/zipper)
                 (z/goNext) (z/goNext) (z/goNext) (z/goNext) (z/goNext) (z/goNext)
                 (z/root))
             => {:node  nodeA
                 :path  [:nil]
                 :left  [:nil]
                 :right [:nil]}
             (-> nodeA
                 (z/zipper)
                 (z/root))
             => {:node  nodeA
                 :path  [:nil]
                 :left  [:nil]
                 :right [:nil]}))

(facts "about `goNext`"
       (fact "end? is true after lonelyNode"
             (z/end? (z/goNext (z/zipper lonelyRoot)))
             => true)
       (fact "it moves to nodeB"
             (-> nodeA
                 (z/zipper)
                 (z/goNext))
             => {:node  nodeB
                 :path  [nodeA :nil]
                 :left  [[:nil]]
                 :right [nodeC nodeD [:nil]]})
       (fact "it moves to nodeE"
             (-> nodeA
                 (z/zipper)
                 (z/goNext) (z/goNext))
             => {:node  nodeE
                 :path  [nodeB nodeA :nil]
                 :left  [[[:nil]]]
                 :right [nodeF [nodeC nodeD [:nil]]]})
       (fact "it moves to nodeF"
             (-> nodeA
                 (z/zipper)
                 (z/goNext) (z/goNext) (z/goNext))
             => {:node  nodeF
                 :path  [nodeB nodeA :nil]
                 :left  [[[:nil]] nodeE]
                 :right [[nodeC nodeD [:nil]]]})
       (fact "it moves to nodeC"
             (-> nodeA
                 (z/zipper)
                 (z/goNext) (z/goNext) (z/goNext) (z/goNext))
             => {:node  nodeC
                 :path  [nodeA :nil]
                 :left  [[:nil] nodeB]
                 :right [nodeD [:nil]]})
       (fact "it moves to nodeG"
             (-> nodeA
                 (z/zipper)
                 (z/goNext) (z/goNext) (z/goNext) (z/goNext) (z/goNext))
             => {:node  nodeG
                 :path  [nodeC nodeA :nil]
                 :left  [[[:nil] nodeB]]
                 :right [[nodeD [:nil]]]})
       (fact "it moves to nodeD"
             (-> nodeA
                 (z/zipper)
                 (z/goNext) (z/goNext) (z/goNext) (z/goNext) (z/goNext) (z/goNext))
             => {:node  nodeD
                 :path  [nodeA :nil]
                 :left  [[:nil] nodeB nodeC]
                 :right [[:nil]]}))