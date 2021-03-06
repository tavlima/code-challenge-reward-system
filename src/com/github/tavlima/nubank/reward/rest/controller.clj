(ns com.github.tavlima.nubank.reward.rest.controller
  (:require [ring.util.response :as resp]
            [io.pedestal.http :as http]
            [com.github.tavlima.nubank.reward.rest.adapter-invitations :as adapter]
            [markdown.core :as md]
            [clojure.java.io :as io]))

(defn- bad-request []
  {:status  400
   :headers {}
   :body    "Bad Request"})

(defn- not-found []
  (resp/not-found "Not Found"))

(def readme-file
  (io/resource "README.md"))

(defn home-page []
  (resp/response (md/md-to-html-string (slurp readme-file))))

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