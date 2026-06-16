(ns bibi-zen-front.utils)

(defn parse-date [value]
  (when (string? value)
    (let [parsed (js/Date. value)]
      (when-not (js/isNaN (.getTime parsed))
        parsed))))

(defn store-tariff-active? [store]
  (let [current-tariff (:current_tariff store)
        ended-at (:ended_at current-tariff)
        planned-until (:planned_until current-tariff)
        planned-date (parse-date planned-until)]
    (cond
      (nil? current-tariff) false
      (some? ended-at) false
      planned-date (>= (.getTime planned-date) (.getTime (js/Date.)))
      :else true)))
