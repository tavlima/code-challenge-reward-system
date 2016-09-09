(ns com.github.tavlima.nubank.reward.rest.adapter-invitations
  (require [ring.util.codec :as codec]
           [com.github.tavlima.nubank.reward.invitations.port :as invitation]))

(defonce tree (atom (invitation/create-tree)))

(defn invite [inviter invitee]
  (swap! tree invitation/invite (codec/url-decode inviter) invitee))

(defn ranking []
  (invitation/ranking @tree))

(defn get-user [userId]
  (invitation/get-user @tree (codec/url-decode userId)))