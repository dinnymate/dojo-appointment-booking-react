(ns dojo-appointment.client.react
  (:require-macros [dojo-appointment.client.react :refer [defcomponent]])
  (:require [react :default react]
            [react-dom :default react-dom]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            ["@emotion/core" :as emotion]
            [goog.object]))

(def render react-dom/render)
(def use-state react/useState)
(def use-context react/useContext)
(defn use-effect
  ([fn]
    (use-effect fn []))
  ([fn deps]
   (react/useEffect fn (clj->js deps))))
(def use-ref react/useRef)
(def create-context react/createContext)
(def user-reducer react/userReducer)

(def children-only (.-only react/Children))

(defn obj->map [o]
  (let [map (atom {})]
    (goog.object/forEach o
      (fn [val key]
        (swap! map assoc (csk/->kebab-case-keyword key) val)))
    @map))

(defn map->obj [m]
  (apply js-obj (mapcat #(update % 0 csk/->camelCaseString) (seq m))))

(defn css [& args]
  (apply emotion/css (map #(clj->js (cske/transform-keys identity %)) args)))

(defn style
  ([styles props]
   (merge {:css [styles (:css props)]} props))
  ([styles]
    (style styles {})))


(defn e [component & props+children]
  (let [has-props (map? (first props+children))
        props (if has-props (first props+children) {})
        css-props (merge
                    props
                    (if (contains? props :css)
                      {:css (css (:css props))}))
        children (if has-props (rest props+children) props+children)
        props-transfromed (if (or (keyword? component) (string? component))
                            (map->obj (into {} (map (fn [[k v]] [k (clj->js v)]) css-props)))
                            (map->obj css-props))
        component-transformed (if (keyword? component)
                                (if (= component :*)
                                  react/Fragment
                                  (name component))
                                component)]
    (apply emotion/jsx component-transformed props-transfromed (map clj->js children))))


(defn merge-props [element new-props]
  (let [key (aget element "key")
        ref (aget element "ref")
        props (obj->map (aget element "props"))]
    (e (.-type element) (merge (if key {:key key}) (if ref {:ref ref}) props new-props))))


(defn use-atom [a]
  (let [a (if (instance? cljs.core.Atom a) a (atom a))
        [atom _] (use-state a)
        [_ update-value] (use-state @a)]
    (use-effect
      (fn []
        (let [k (gensym "use-atom")]
          (add-watch a k
            (fn [_ _ _ new-state]
              (update-value (constantly new-state))))
          (fn []
            (remove-watch a k)))))
    atom))


(defn provider [context]
  (.-Provider context))

(defn Consumer [context]
  (.Consumer context))

(defcomponent global {:keys [styles]}
  (e emotion/Global {:styles (clj->js (cske/transform-keys identity styles))}))

