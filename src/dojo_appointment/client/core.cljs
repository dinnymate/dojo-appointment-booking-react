(ns dojo-appointment.client.core
  (:require [dojo-appointment.client.react :as r :refer [defcomponent e ]]
            [dojo-appointment.client.components.app :refer [app]]))

(defn ^:dev/after-load mount []
  (r/render (e app) (.getElementById js/document "app")))

(defn ^:dev/before-load clear-console []
  (.clear js/console))

(defn -main []
  (mount))

