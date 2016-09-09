(ns com.github.tavlima.nubank.reward.rest.adapter-invitations
  (require [ring.util.codec :as codec]
           [com.github.tavlima.nubank.reward.persistence.port :as persistence]
           [com.github.tavlima.nubank.reward.invitations.port :as invitation]))

(defn invite [inviter invitee]
  (persistence/update-tree (fn [tree]
                        (invitation/invite tree (codec/url-decode inviter) invitee))))

(defn ranking []
  (invitation/ranking (persistence/get-tree)))

(defn get-user [userId]
  (invitation/get-user (persistence/get-tree) (codec/url-decode userId)))