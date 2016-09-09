(ns com.github.tavlima.nubank.reward.rest.core.controller
  (:require [ring.util.response :as resp]
            [io.pedestal.http :as http]
            [com.github.tavlima.nubank.reward.rest.adapter-invitations :as adapter]
            [com.github.tavlima.nubank.reward.util :as util]))

(defn- bad-request []
  {:status  400
   :headers {}
   :body    "Bad Request"})

(defn- not-found []
  (resp/not-found "Not Found"))

(defn home-page []
  (resp/response "Nubank's Reward System"))

(defn get-ranking []
  (-> (adapter/ranking)
      (http/json-response)))

(defn get-user [uid]
  (let [user (adapter/get-user uid)]
    (if (nil? user)
      (not-found)
      (http/json-response {:id      (:id user)
                           :score   (:score user)
                           :invited (map :id (:invited user))}))))

(defn invite [inviter invitee]
  (if (string? invitee)
    (do (adapter/invite inviter invitee)
        (get-user inviter))
    (bad-request)))