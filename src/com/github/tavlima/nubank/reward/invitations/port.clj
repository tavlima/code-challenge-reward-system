(ns com.github.tavlima.nubank.reward.invitations.port
  (require [com.github.tavlima.nubank.reward.invitations.controller :as c]))

(defn invite [tree inviterId inviteeId]
  (c/invite tree inviterId inviteeId))

(defn create-tree []
  (c/create-tree))

(defn ranking [tree]
  (c/ranking tree))

(defn get-user [tree userId]
  (c/get-user tree userId))