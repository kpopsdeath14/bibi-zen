(ns bibi-zen-front.events.all-actual-policies-get
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.db :refer [app-state]]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]]
            )
  )

(defn all_actual_policies_get_handler [[ok? response]] 
  (swap! app-state assoc :actual_policies
         (if ok?
           (mapv (fn [policy] (:data policy)) response)
           []
           )
         )
  )



(defn all_actual_policies_get []
  (let []
    (http/ajax-request-with-headers
     {:uri (api_uri_maker "policies-all-actual-get")
      :method :post
      :params {}
      :handler all_actual_policies_get_handler
      :format (ajax/json-request-format)
      :response-format (ajax/json-response-format {:keywords? true})})))
