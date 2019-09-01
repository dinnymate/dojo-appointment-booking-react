(ns dojo-appointment.server.api.booking
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :as r]
            [clojure.spec.alpha :as s]
            [clojure.string :as str])
  (:import (java.util UUID)))

(s/def ::start (s/and int? #(<= 0 % 24)))
(s/def ::end (s/and int? #(<= 0 % 24)))
(s/def ::name (s/and string? (complement str/blank?)))
(s/def ::uuid uuid?)

(s/def ::booking-data
  (s/and
    (s/keys :req-un [::name ::start ::end])
    #(< (% :start) (% :end))))

(s/def ::booking (s/merge (s/keys :req-un [::uuid]) ::booking-data))
(s/def ::bookings (s/* ::booking))

(defn- booking-collides [bookings {:keys [start end]}]
  (not= bookings
    (take-while #(or (>= (% :start) end) (>= start (% :end))) bookings)))

(defn get-booking [store uuid]
  (r/ok (first (filter #(= (% :uuid) uuid) (@store :bookings)))))

(defn list-bookings [store]
  (r/ok {:bookings (sort-by :start (@store :bookings))}))

(defn create-booking [store booking]
  (let [booking (assoc booking :uuid (UUID/randomUUID))
        [old new] (swap-vals! store update-in [:bookings]
                    (fn [bookings]
                      (if (booking-collides bookings booking)
                        bookings
                        (conj bookings booking))))]
    (if (= old new)
      (r/conflict "Booking confilct!")
      (r/ok booking))))

(defn delete-booking [store uuid]
  (swap! store update-in [:bookings]
    (fn [bookings]
      (filter #(not= (% :uuid) uuid) bookings))))

(defn booking-controller [store]
  (context "/bookings" []
    (GET "/" []
      :return (s/keys :req-un [::bookings])
      (list-bookings store))

    (GET "/:uuid" []
      :path-params [uuid :- ::uuid]
      :return ::booking
      (get-booking store uuid))

    (POST "/" []
      :body [body ::booking-data]
      :responses {200 {:schema ::booking}
                  409 {:schema string?}}
      (create-booking store body))

    (DELETE "/:uuid" []
      :path-params [uuid :- ::uuid]
      (delete-booking store uuid))))