(ns dojo-appointment.client.react
  (:require [dojo-appointment.client.destructure :refer [let+]]))

(defmacro defcomponent [var props-binding & body]
  `(do (defn ~var [props#]
         (let+ [~props-binding (obj->map props#)]
           ~@body))
       (aset ~var "displayName" ~(name var))))

(defmacro component [props-binding & body]
  `(fn [props#]
     (let+ [~props-binding (obj->map props#)]
       ~@body)))
