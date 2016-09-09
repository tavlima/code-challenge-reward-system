(ns com.github.tavlima.nubank.reward.rest.adapter-invitations
  (require [com.github.tavlima.nubank.reward.invitations.port :as invitation]))

(defonce tree (atom (invitation/create-tree)))

(defn invite [inviter invitee]
  (swap! tree invitation/invite (ring.util.codec/url-decode inviter) invitee))

(defn ranking []
  (invitation/ranking @tree))

(defn get-user [userId]
  (invitation/get-user @tree (ring.util.codec/url-decode userId)))