(ns dojo-appointment.server.system
  (:require [integrant.core :as ig]
            [org.httpkit.server :refer [run-server]]
            [dojo-appointment.server.api.core :refer [router]]
            [dojo-appointment.server.api.booking :refer [booking-controller]]))


(def store-schema {:bookings []
                   :slots []})

(def default-config
  {:http {:port 80}})

(defn create-system [config]
  (let [{:keys [http]} (merge default-config config)]
    {::http (assoc http :handler (ig/ref ::router))

     ::router {:controllers (ig/refset ::controller)
               :version (ig/ref ::version)}

     ::version {}

     ::store {:schema store-schema}

     [:controller/booking ::controller] {:store (ig/ref ::store)}}))


(defmethod ig/init-key ::http [_ opts]
  (run-server (:handler opts) (dissoc opts :handler)))

(defmethod ig/halt-key! ::http [_ server]
  (server))

(defmethod ig/init-key ::router [_ {:keys [controllers version]}]
  (router controllers version))

(defmethod ig/init-key ::version  [_ _]
  "0.0.2")

(defmethod ig/init-key ::store  [_ {:keys [schema]}]
  (atom schema))

(defmethod ig/halt-key! ::store [_ store]
  store)

(defmethod ig/resume-key ::store [_ {:keys [schema]} {old-schema :schema} old-imp]
  (if (= schema old-schema)
    old-imp
    (atom schema)))

;controllers

(defmethod ig/init-key :controller/booking  [_ {:keys [store]}]
  (booking-controller store))