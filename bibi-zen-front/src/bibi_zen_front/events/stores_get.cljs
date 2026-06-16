(ns bibi-zen-front.events.stores-get
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.db :refer [app-state]]
            [reagent.cookies :as cookies]
[bibi-zen-front.apiurimaker :refer [api_uri_maker]]))

(defn- normalize-stores-response [response]
  (cond
    (map? response) [response]
    (sequential? response)
    (let [items (vec response)]
      (cond
        (empty? items) []
        (every? #(and (map? %) (contains? % :data)) items) (mapv :data items)
        (every? map? items) items
        :else []))
    :else []))

(defn stores_get_handler [[ok? response]]
  (let [stores (if ok? (normalize-stores-response response) [])]
    (swap! app-state
           (fn [state]
             (let [current-uid (:store_uid (:current-store state))
                   updated-current (when current-uid
                                     (first (filter #(= (:store_uid %) current-uid) stores)))]
               (-> state
                   (assoc :stores stores)
                   (cond-> (nil? (:current-store state))
                     (assoc :current-store (first stores)))
                   (cond-> updated-current
                     (assoc :current-store updated-current))))))))



(defn stores_get []
  (let []
    (http/ajax-request-with-headers
     {:uri (api_uri_maker "stores-get")
      :method :post
      :params {}
      :handler stores_get_handler
      :format (ajax/json-request-format)
      :response-format (ajax/json-response-format {:keywords? true})
      }
     )
    )
  )
