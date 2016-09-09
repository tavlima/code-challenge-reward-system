(ns com.github.tavlima.nubank.reward.persistence.controller
  (:require [com.github.tavlima.nubank.reward.invitations.port :as invitation]))

(defonce atomic-tree (atom (invitation/create-tree)))

(defn get-tree []
  @atomic-tree)

(defn update-tree [fn]
  (swap! atomic-tree fn))