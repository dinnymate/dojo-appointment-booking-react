(ns dojo-appointment.server.api.core
  (:require [compojure.api.sweet :as c]
            [compojure.api.validator :refer [validate]]
            [compojure.core :as cc]
            [compojure.route :as cr]
            [ring.util.http-response :as r]
            [clojure.string :refer [join]]
            [hiccup.page :refer [html5 include-css include-js]]
            [ring.swagger.swagger-ui :refer [swagger-ui]]))


(defn viewport [opts]
  [:meta {:name :viewport
          :content (join ","
                     (map
                       (fn [[k v]]
                         (str (name k) "=" (name v))) opts))}])

(defn index [version]
  (html5 {}
    [:head
     [:meta {:charset "utf-8"}]
     [:base {:href "/"}]
     (viewport {:width :device-width
                :initial-scale "1.0"
                :maximum-scale "1.0"
                :user-scalable :no})
     [:title "Appointment Booking"]
     (include-css
       (str "https://fonts.googleapis.com/"
         "css?family=Source+Sans+Pro:300,400,600,700,300italic,400italic,600italic"))]
    [:body
     [:div#app {:data-version version}]
     (include-js "assets/app.js")]))


(defn router [controllers version]
  (c/routes
    (c/context "/api" []
      (validate
        (apply c/api
          {:coercion :spec
           :swagger {:ui "docs"
                     :spec "/swagger.json"
                     :data {:basePath "/api"}}}
          controllers))
      (cc/rfn [] (r/not-found "")))
    (cr/resources "/assets")
    (cc/GET "/favicon.ico" []
      (r/resource-response "/favicon.ico"))
    (cc/rfn [] (index version))))