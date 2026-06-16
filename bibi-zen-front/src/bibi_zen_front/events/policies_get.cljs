(ns bibi-zen-front.events.policies-get
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.db :refer [app-state]]
            [reagent.cookies :as cookies]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]]
            )
  )

(defn policies_get_handler [[ok? response]]
  (swap! app-state assoc :policies
         (if ok?
           (mapv (fn [product] (:data product)) response)
           []
           )
         )
  (swap! app-state assoc :policies_menu_open? (not (empty? (:policies @app-state))))
  )



(defn policies_get []
  (let []
    (http/ajax-request-with-headers
     {:uri (api_uri_maker "policies-get")
      :method :post
      :params {}
      :handler policies_get_handler
      :format (ajax/json-request-format)
      :response-format (ajax/json-response-format {:keywords? true})})))
