(ns com.github.tavlima.nubank.reward.invitations.t-port
  (:use clojure.test
        midje.sweet
        com.github.tavlima.nubank.reward.invitations.port))

(facts "about `create-tree`"
       (fact "it just delegates to controller/create-tree"))

(facts "about `invite`"
       (fact "it just delegates to controller/invite"))

(facts "about `ranking`"
       (fact "it just delegates to controller/ranking"))

(facts "about `get-user`"
       (fact "it just delegates to controller/get-user"))
