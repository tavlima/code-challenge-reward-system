(ns com.github.tavlima.nubank.reward.rest.core.port
  (:require [com.github.tavlima.nubank.reward.rest.core.controller :as controller]))

(defn about-page [_]
  (controller/about-page))

(defn home-page [_]
  (controller/home-page))

(defn get-user [request]
  (controller/get-user (get-in request [:path-params :uid])))

(defn invite [request]
  (controller/invite (get-in request [:path-params :uid]) (get-in request [:form-params :invitee])))

(defn get-ranking [_]
  (controller/get-ranking))