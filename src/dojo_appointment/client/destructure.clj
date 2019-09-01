(ns dojo-appointment.client.destructure)

(defn associative-destructure [to from]
  (let [default (get to :or {})
        mappings
        (reduce-kv
          (fn [a k v]
            (cond
              (#{:as :or :&} k) a
              (and (keyword? k) (= (keyword (name k)) :keys))
              (concat a (mapv #(vector % (keyword (namespace k) (name %))) v))

              (and (keyword? k) (= (keyword (name k)) :syms))
              (concat a (mapv #(vector % (symbol (namespace k) (name %))) v))

              (= k :strs) (concat a (mapv #(vector % (str (name %))) v))
              :else (conj a [k v]))) [] (dissoc to :or))

        rest `(dissoc ~from ~@(map second mappings))
        bindings (mapv (fn [[k v]] [k `(~from ~v ~@`(~(default k)))]) mappings)]
    (->> bindings
         (#(if (contains? to :as) (conj % [(:as to) from]) %))
         (#(if (contains? to :&) (conj % [(:& to) rest]) %)))))


(defn sequential-destructure [to from]
  (let [has-as (some #(= :as %) to)
        has-rest (some #(= '& %) to)
        length (+ (count to) (if has-rest -2 0) (if has-as -2 0))]
    (concat
      (mapv
        (fn [k v]
          [k `(~from ~v)])
        (take length to) (iterate inc 0))
      (if has-rest
        [(to (+ length 1)) `(into [] (drop ~length ~from))] [])
      (if has-as
        [(to (+ length (if has-rest 2 0) 1)) from] []))))


(defn destructure+ [bindings]
  (into []
    (apply concat
      (loop [bindings (partition 2 bindings)]
        (let [destructured
              (apply concat
                (for [[to from] bindings]
                  (if (simple-symbol? to)
                    (list [to from])
                    (let [from-sym (gensym "from__")]
                      (cons [from-sym from]
                            (cond
                              (map? to) (associative-destructure to from-sym)
                              (vector? to) (sequential-destructure to from-sym)
                              :else (throw (Exception. "Destructuring failed."))))))))]
          (if (every? #(simple-symbol? (first %)) destructured)
            destructured
            (recur destructured)))))))

(defmacro let+ [bindings & body]
  `(let* ~(destructure+ bindings) ~@body))

