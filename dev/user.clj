(ns user
  (:require [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as shadow-server]
            [dojo-appointment.server.system :as sys]
            [integrant.repl :refer [go reset halt]]))


(integrant.repl/set-prep! (partial sys/create-system {:http {:port 8080 :ip "0.0.0.0"}}))


(defn get-service [service]
  (integrant.repl.state/system service))

(defn clear-store []
  (reset! (get-service ::sys/store) sys/store-schema))


(defn cljs-watch []
  (shadow-server/start!)
  (shadow/watch :app))

(defn cljs-stop []
  (shadow/stop-worker :app))

(defn cljs-repl []
  (shadow/repl :app))