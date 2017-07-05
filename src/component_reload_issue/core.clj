(ns component-reload-issue.core
  (:gen-class)
  (:require [cheshire.core :as json]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [defroutes GET routes]]
            [org.httpkit.server :as http-kit]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; API
;;
(defroutes demo-api-routes
  (GET "/info" [] {:status 200
                   :headers {"Content-type" "application/json"}
                   :body (json/generate-string {:status "OK"})}))

(defn- wrap-with-deps [f deps]
  (fn [req]
    (f (merge req [:system-deps deps]))))

(defn make-handler [routes deps]
  (-> routes
      (wrap-with-deps deps)))

(defrecord DemoRoutes []
  component/Lifecycle

  (start [this]
    (println "Start: routes")
    (assoc this :routes (make-handler demo-api-routes {})))

  (stop [this]
    (println "Stop: routes")
    (dissoc this :routes)))

(defn demo-routes []
  (map->DemoRoutes {}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Server
;;
(defrecord DemoServer [server demo-routes]
  component/Lifecycle

  (start [component]
    (println "Start: server")
    (let [server (http-kit/run-server (:routes demo-routes) {:port 8000})]
      (assoc component :server server)))

  (stop [component]
    (println "Stop: server")
    (server)
    (dissoc component :server)))

(defn demo-server
  []
  (map->DemoServer {}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; System
;;
(defn system
  [with-server]
  (if with-server
    (component/system-map
     :demo-routes (demo-routes)
     :demo-server (component/using (demo-server) [:demo-routes]))
    (component/system-map
     :demo-routes (component/using (demo-routes) []))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; lein-ring interop. (not working!)
;;
(defn ring-init
  []
  (let [with-server false
        demo-system (system with-server)
        system-map (component/start demo-system)]

    (println "lein-ring routes:" (:routes (:demo-routes system-map)))
    (def ring-handler (:routes (:demo-routes system-map)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Not used for the purpose of this issue report
;;
(defn -main
  [& args]
  (let [with-server true
        system-map (component/start (system with-server))]
    (println "-main routes:" (:routes (:demo-routes system-map)))))
