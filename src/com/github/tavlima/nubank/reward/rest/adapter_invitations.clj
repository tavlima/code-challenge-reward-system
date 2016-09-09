(ns com.github.tavlima.nubank.reward.rest.adapter-invitations
  (require [com.github.tavlima.nubank.reward.invitations.port :as invitation]))

(defonce tree (atom (invitation/create-tree)))

(defn invite [inviter invitee]
  (swap! tree invitation/invite (Integer. inviter) (Integer. invitee)))

(defn ranking []
  (invitation/ranking @tree))

(defn get-user [userId]
  (let [user (invitation/get-user @tree (Integer. userId))]
    (if (nil? user)
      {}
      {:id (:id user)
       :score (:score user)
       :invited (map :id (:invited user))})))