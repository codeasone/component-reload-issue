(defproject component-reload-issue "0.1.0-SNAPSHOT"
  :description "Demonstrates component and lein-ring interop issue"
  :url "https://github.com/codeasone/component-reload-issue"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure        "1.8.0"]
                 [cheshire                   "5.7.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.taoensso/timbre        "4.7.4"]
                 [compojure                  "1.5.2"]
                 [http-kit                   "2.2.0"]
                 [ring/ring-defaults         "0.2.3"]
                 [ring/ring-json             "0.4.0"]]

  :profiles {:dev {:plugins [[lein-ring "0.11.0"]]}}

  :ring {:handler component-reload-issue.core/ring-handler
         :init component-reload-issue.core/ring-init
         :port 8000
         :auto-reload? true}

  :main ^:skip-aot component-reload-issue.core)
