(ns dojo-appointment.client.components.app
  (:require [dojo-appointment.client.react :as r :refer [defcomponent e style global]]
            [promesa.core :as p]
            [dojo-appointment.client.style :refer [global-styles]]
            [dojo-appointment.client.http-client :refer [http]]
            [clojure.string :as str]))

(declare booking-controller)
(declare booking-view)

(defcomponent app _
  (e :div (style [{:padding "20px" :height "100%" :background "#f0f0f0" :overflow :hidden} [{:overflowY :auto}]])
    (e global {:styles global-styles})
    (e :h1 (style [{:fontSize "28px" :marginBottom "20px"}]) "Appointment Booking")
    (e booking-controller
      #(e booking-view (style [{:width "500px" :maxWidth "100%"}] %)))))


(defcomponent booking-controller2
  {:keys [children refresh-interval] :or {refresh-interval 10000}}
  ((let [state (r/use-atom {:bookings [] :error nil :action nil})]
     (r/use-effect
       (fn []
         (case ((@state :action) :type)
           (:fetch :fetch-fg)
           (->
             (http {:method :get :url "/api/bookings"})
             (p/then #(swap! state merge (% :data) {:action nil})))
           :delete
           (->
             (http {:method :delete :url (str "/api/bookings/"
                                           (get-in @state [:action :payload]))})
             (p/then #(swap! state merge {:error nil :action {:type :fetch-fg}})))
           :create
           (->
             (http {:method :post :url "/api/bookings" :data (get-in @state [:action :payload])})
             (p/then #(swap state merge {:error nil :action {:type :fetch-fg}})))))
       [(@state :action)])
     (children (merge (select-keys @state [:bookings :error])
                 {:loading (and
                             (@state :action)
                             (not= (@state :action) :fetch-fg))})))))


(defcomponent booking-controller
  {:keys [children refresh-interval] :or {refresh-interval 10000}}

  (let [state (r/use-atom {:bookings [] :loading true :error nil :offline false})
        fetch-bookings
        (fn []
          (->
            (http {:method :get :url "/api/bookings/"})
            (p/then #(swap! state merge {:offline false} (% :data)))
            (p/catch #(swap! state assoc :offline true))))

        delete-bookings
        (fn [uuid]
          (swap! state assoc :loading true)
          (->
            (http {:method :delete :url (str "/api/bookings/" uuid)})
            (p/then fetch-bookings)
            (p/then #(swap! state assoc :loading false))
            (p/catch #(swap! state merge {:loading false
                                          :error "Failed to delete bookings"}))))

        create-booking
        (fn [data]
          (swap! state assoc :loading true)
          (->
            (http {:method :post :url "/api/bookings/" :data data})
            (p/then fetch-bookings)
            (p/then #(swap! state merge {:loading false :error nil}))
            (p/catch
              #(swap! state merge
                 {:loading false
                  :error (if (= 409 (% :status)) "Collision!" "Failed to create booking!")}))))]
    (r/use-effect
      (fn []
        (swap! state assoc :loading true)
        (.finally
          (fetch-bookings)
          (swap! state assoc :loading false))
        js/undefined)
      [refresh-interval])

    (if children
      (children (merge @state {:handle-delete delete-bookings
                               :handle-create create-booking
                               :handle-refresh (fn []
                                                 (swap! state assoc :loading true)
                                                 (.finally
                                                   (fetch-bookings)
                                                   #(swap! state assoc :loading false)))})))))


(def booking-styles
  {:box {:border "1px solid #e0e0e0"
         :border-top "5px solid #7777ff"
         :box-shadow "1px 1px 3px rgba(0,0,0,0.1)"
         :padding "0 10px 10px 10px"
         :background "#fff"
         :border-radius "5px"
         :position :relative
         :overflow :hidden}
   :loading {:position :absolute
             :top 0
             :left 0
             :right 0
             :bottom 0
             :background "rgba(0,0,0,0.25)"
             :display :flext
             :alignItems :center
             :justifyContent :center}
   :error {:background "#dd0000"
           :color :white
           :border "1px solid #aa0000"
           :borderRadius "5px"
           :padding "10px"
           :margin "10px"}

   :input {:border "1px solid #ccc"
           ":focus" {:border "1px solid #7777ff"
                     :z-index 10}
           ":last-child" {:marginRight "0"}
           :lineHeight "20px"
           :fontSize "14px"
           :padding "6px 12px"
           :marginRight "-1px"}
   :li {"span" {:padding "0 10px" :line-height "20px"}
        :display :flex
        :padding "6px 12px"
        :borderBottom "1px solid #ccc"
        ":last-child" {:border :none}
        "button" {:color :red :fontSize "28px" :fontWeight "900" :line-height "20px"}}})

(defn bind-input
  ([a keyseq]
   (bind-input a keyseq {}))
  ([a keyseq props]
   (merge {:value (get-in @a keyseq) :on-change #(swap! a assoc-in keyseq (.-value (.-target %)))} props)))

(defcomponent booking-view {:keys [bookings error offline
                                   loading handle-refresh handle-create handle-delete class-name]}
  (let [default-state {:start "0" :end "0" :name ""}
        state (r/use-atom default-state)]
    (e :div (style [(booking-styles :box)] {:class-name class-name})

      (if loading
        (e :div (style (booking-styles :loading))
          (e :span "Loading...")))
      (e :div (style {:marginBottom "5px"})
        (e :h3 (style {:fontSize "22px" :padding "5px"}) "Appointments"
          (e :button (style {:float :right} {:on-click handle-refresh}) "⟳")))
      (if offline (e :span (style (booking-styles :error)) "Offline"))
      (if error (e :div (style (booking-styles :error)) error))

      (e :ul (style {:marginBottom "10px"})
        (for [b bookings]
          (e :li (style (booking-styles :li) {:key (b :uuid)})

            (e :span (b :start) ":00")
            (e :span (b :end) ":00")
            (e :span (style {:flexGrow 1}) (b :name))
            (e :button {:on-click #(handle-delete (b :uuid))} "×"))))
      (e :form (style [{:display :flex :width "100%"}] {:on-submit #(do (.preventDefault %)
                                                                        (if-not (str/blank? (@state :name))
                                                                          (p/then (handle-create
                                                                                    {:start (int (@state :start))
                                                                                     :end (int (@state :end))
                                                                                     :name (@state :name)})
                                                                            (reset! state default-state))))})
        (e :select (style (booking-styles :input) (bind-input state [:start]))
          (for [x (range 0 25)] (e :option {:key x :value x} x ":00")))
        (e :select (style (booking-styles :input) (bind-input state [:end]))
          (for [x (range 0 25)] (e :option {:key x :value x} x ":00")))
        (e :input (style [(booking-styles :input) {:flexGrow 1 :flexShrink 1 :minWidth "0px"}] (bind-input state [:name] {:type :text})))
        (e :button (style [(booking-styles :input)
                           {:background "#55bb33" :border-color "#55bb33" :font-size "20px" :color "#fff"}] {:type :submit}) "+")))))