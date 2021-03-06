(ns com.github.tavlima.nubank.reward.main.route
  (:require [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http :as http]
            [com.github.tavlima.nubank.reward.rest.port :as port]))

(def common-interceptors [(body-params/body-params) http/html-body])

(def wrap (partial conj common-interceptors))

;; Tabular routes
(def routes #{["/"                   :get   (wrap #'port/home-page)    :route-name :home-page]
              ["/ranking"            :get   (wrap #'port/get-ranking)  :route-name :get-ranking]
              ["/users/:uid"         :get   (wrap #'port/get-user)     :route-name :get-user]
              ["/users/:uid/invite"  :post  (wrap #'port/invite)       :route-name :invite]})
