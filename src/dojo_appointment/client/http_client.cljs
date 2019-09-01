(ns dojo-appointment.client.http-client
  (:require [axios :default axios]
            [promesa.core :as p]))


(defn http [config]
  (->
    (axios (clj->js config))
    (p/then #(js->clj % :keywordize-keys true))
    (p/catch #(throw (js->clj (aget % "response") :keywordize-keys true)))))
