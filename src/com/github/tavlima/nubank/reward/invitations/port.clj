(ns com.github.tavlima.nubank.reward.invitations.port
  (require [com.github.tavlima.nubank.reward.invitations.controller :as controller]))

(defn invite [tree inviterId inviteeId]
  (controller/invite tree inviterId inviteeId))

(defn create-tree []
  (controller/create-tree))

(defn ranking [tree]
  (controller/ranking tree))

(defn get-user [tree userId]
  (controller/get-user tree userId))