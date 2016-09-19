(ns com.github.tavlima.nubank.reward.tree.domain.t-location
  (use midje.sweet)
  (:require [com.github.tavlima.nubank.reward.tree.domain
             [zippable :as z]
             [location :as l]
             [t-zippable :as tz]]))

(def nodeG {:v :g :c []})
(def nodeE {:v :e :c []})
(def nodeF {:v :f :c []})
(def nodeB {:v :b :c [nodeE, nodeF]})
(def nodeC {:v :c :c [nodeG]})
(def nodeD {:v :d :c []})
(def nodeA {:v :a :c [nodeB, nodeC, nodeD]})
(def lonelyRoot {:v :lonely :c []})

(facts "about `node`"
       (fact "it returns the :node at any location"
             (l/node (z/zipper {:v "my node"}))
             => {:v "my node"}

             (l/node (z/zipper {:v "my node 2"}))
             => {:v "my node 2"}))

(facts "about `end?`"
       (fact "it returns true if :node is :nil and false, otherwise"
             (l/end? (z/zipper :nil))
             => true

             (l/end? (z/zipper :whatever))
             => false))

(facts "about `replace`"
       (fact "it replaces the node at current location by the one passed as argument"
             (l/replace (z/zipper {:v 1}) {:anothervalue 2})
             => {:node  {:anothervalue 2}
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}))

(facts "about `update`"
       (fact "it calls the function, passing the current node, and updates only the :node value"
             (l/update (z/zipper {:v 1}) #(assoc % :v (inc (:v %))))
             => {:node  {:v 2}
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}))

(facts "about `append-child`"
       (fact "it returns an updated location with the new child"
             (-> (z/zipper nodeD)
                 (l/append-child nodeE))
             => {:node  {:v :d :c [{:v :e :c []}]}
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}
             (-> (z/zipper nodeC)
                 (l/append-child nodeD))
             => {:node  {:v :c :c [{:v :g :c []}
                                              {:v :d :c []}]}
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}))

(facts "about `down`"
       (fact "it returns nil if the node has no child"
             (-> (z/zipper nodeG)
                 (l/down))
             => nil)
       (fact "it moves to nodeB"
             (-> (z/zipper nodeA)
                 (l/down))
             => {:node  nodeB
                 :path  [nodeA :nil]
                 :s-left  [[:nil]]
                 :s-right [nodeC nodeD [:nil]]})
       (fact "it moves to nodeE"
             (-> (z/zipper nodeA)
                 (l/down)
                 (l/down))
             => {:node  nodeE
                 :path  [nodeB nodeA :nil]
                 :s-left  [[[:nil]]]
                 :s-right [nodeF [nodeC nodeD [:nil]]]}))

(facts "about `right`"
       (fact "it returns nil if the node has no right siblings"
             (-> (z/zipper nodeC)
                 (l/down)
                 (l/right))
             => nil)
       (fact "it moves to nodeC"
             (-> (z/zipper nodeA)
                 (l/down)
                 (l/right))
             => {:node  nodeC
                 :path  [nodeA :nil]
                 :s-left  [[:nil] nodeB]
                 :s-right [nodeD [:nil]]})
       (fact "it moves to nodeF"
             (-> (z/zipper nodeA)
                 (l/down)
                 (l/down)
                 (l/right))
             => {:node  nodeF
                 :path  [nodeB nodeA :nil]
                 :s-left  [[[:nil]] nodeE]
                 :s-right [[nodeC nodeD [:nil]]]}))

(facts "about `up`"
       (fact "it returns nil if already at the root"
             (l/up (z/zipper lonelyRoot)) => nil)
       (fact "it moves from B to A"
             (-> (z/zipper nodeA) (l/next) (l/up))
             => (z/zipper nodeA))
       (fact "it moves from F to B"
             (-> (z/zipper nodeA) (l/next) (l/next) (l/next) (l/up))
             => {:node  nodeB
                 :path  [nodeA :nil]
                 :s-left  [[:nil]]
                 :s-right [nodeC nodeD [:nil]]})
       (fact "it preserves the updates"
             (-> (z/zipper nodeC) (l/next) (l/update #(assoc % :updated true)) (l/up))
             => {:node  {:v :c :c [(assoc nodeG :updated true)]}
                 :s-left  [:nil]
                 :path  [:nil]
                 :s-right [:nil]}))

(facts "about `root`"
       (fact "it moves up to the root"
             (-> nodeA
                 (z/zipper)
                 (l/next) (l/next) (l/next) (l/next) (l/next) (l/next)
                 (l/root))
             => {:node  nodeA
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}
             (-> nodeA
                 (z/zipper)
                 (l/root))
             => {:node  nodeA
                 :path  [:nil]
                 :s-left  [:nil]
                 :s-right [:nil]}))

(facts "about `next`"
       (fact "end? is true after lonelyNode"
             (l/end? (l/next (z/zipper lonelyRoot)))
             => true)
       (fact "it moves to nodeB"
             (-> nodeA
                 (z/zipper)
                 (l/next))
             => {:node  nodeB
                 :path  [nodeA :nil]
                 :s-left  [[:nil]]
                 :s-right [nodeC nodeD [:nil]]})
       (fact "it moves to nodeE"
             (-> nodeA
                 (z/zipper)
                 (l/next) (l/next))
             => {:node  nodeE
                 :path  [nodeB nodeA :nil]
                 :s-left  [[[:nil]]]
                 :s-right [nodeF [nodeC nodeD [:nil]]]})
       (fact "it moves to nodeF"
             (-> nodeA
                 (z/zipper)
                 (l/next) (l/next) (l/next))
             => {:node  nodeF
                 :path  [nodeB nodeA :nil]
                 :s-left  [[[:nil]] nodeE]
                 :s-right [[nodeC nodeD [:nil]]]})
       (fact "it moves to nodeC"
             (-> nodeA
                 (z/zipper)
                 (l/next) (l/next) (l/next) (l/next))
             => {:node  nodeC
                 :path  [nodeA :nil]
                 :s-left  [[:nil] nodeB]
                 :s-right [nodeD [:nil]]})
       (fact "it moves to nodeG"
             (-> nodeA
                 (z/zipper)
                 (l/next) (l/next) (l/next) (l/next) (l/next))
             => {:node  nodeG
                 :path  [nodeC nodeA :nil]
                 :s-left  [[[:nil] nodeB]]
                 :s-right [[nodeD [:nil]]]})
       (fact "it moves to nodeD"
             (-> nodeA
                 (z/zipper)
                 (l/next) (l/next) (l/next) (l/next) (l/next) (l/next))
             => {:node  nodeD
                 :path  [nodeA :nil]
                 :s-left  [[:nil] nodeB nodeC]
                 :s-right [[:nil]]}))
