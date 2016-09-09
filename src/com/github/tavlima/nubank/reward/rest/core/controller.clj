(ns com.github.tavlima.nubank.reward.rest.core.controller
  (:require [ring.util.response :as ring-resp]
            [io.pedestal.http :as http]
            [com.github.tavlima.nubank.reward.rest.adapter-invitations :as adapter]))

(defn about-page []
  (ring-resp/response (format "Clojure %s"
                              (clojure-version))))

(defn home-page []
  (ring-resp/response "Nubank Invitation Reward Service"))

(defn get-ranking []
  (-> (adapter/ranking)
      (http/json-response)))

(defn get-user [uid]
  (-> (adapter/get-user uid)
      (http/json-response)))

(defn invite [inviter invitee]
  (do (adapter/invite inviter invitee)
      (get-user inviter)))