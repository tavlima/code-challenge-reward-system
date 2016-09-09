(defproject nubank-invitation-reward-program "0.0.1-SNAPSHOT"
  :description "Nubank's Reward System Code Challenge"
  :url "https://github.com/tavlima/code-challenge-reward-system"
  :license {:name "MIT License"
            :url "https://github.com/tavlima/code-challenge-reward-system/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.pedestal/pedestal.service "0.5.1"]
                 [io.pedestal/pedestal.jetty "0.5.1"]
                 [ch.qos.logback/logback-classic "1.1.7" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.21"]
                 [org.slf4j/jcl-over-slf4j "1.7.21"]
                 [org.slf4j/log4j-over-slf4j "1.7.21"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/tools.cli "0.3.5"]
                 [markdown-clj "0.9.89"]
                 [environ "1.1.0"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :uberjar-name "reward-system-standalone.jar"
  :profiles {:production {:env {:production true}}
             :dev {:aliases {"run-dev" ["trampoline" "run" "-m" "com.github.tavlima.nubank.reward.main.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.1"]
                                  [midje "1.8.3"]]
                   :plugins [[lein-midje "3.2.1"]]}
             :uberjar {:aot [com.github.tavlima.nubank.reward.main.server]}}
  :main ^{:skip-aot true} com.github.tavlima.nubank.reward.main.server)

