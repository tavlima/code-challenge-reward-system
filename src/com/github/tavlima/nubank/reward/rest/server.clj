(ns com.github.tavlima.nubank.reward.rest.server
  (:gen-class) ; for -main method in uberjar
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [io.pedestal.http :as server]
            [io.pedestal.http.route :as route]
            [com.github.tavlima.nubank.reward.rest.bootstrap :as bootstrap]
            [com.github.tavlima.nubank.reward.rest.service :as service]
            [com.github.tavlima.nubank.reward.rest.route :as service-route]))

;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
(defonce runnable-service (server/create-server service/service))

(def cli-options
  [["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Nubank Invitation Reward Service"
        ""
        "Usage: nirs [options] filename"
        ""
        "Arguments:"
        "  filename    File that should be loaded with the initial"
        "              invitations. The file should have one invitation"
        "              per line, in the 'inviterId inviteeId' (without "
        "              the quotes). Both inviterId and inviteeId should"
        "              be integers."
        ""
        "Options:"
        options-summary
        ""]
       (string/join \newline)))

(def prod-server
  runnable-service)

(def dev-server
  (-> service/service ;; start with production configuration
      (merge {:env                     :dev
              ;; do not block thread that starts web server
              ::server/join?           false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::server/routes          #(route/expand-routes (deref #'service-route/routes))
              ;; all origins are allowed in dev mode
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains
      server/default-interceptors
      server/dev-interceptors
      server/create-server))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn run [type server args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 1) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    (let [filename (first arguments)]
      (do (println "Loading input file...")
          (bootstrap/process-file filename)
          (println (format "Creating your %s server..." type))
          (server/start server)))))

(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (run :DEV dev-server args))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (run :PROD prod-server args))
