(ns bibi-zen-front.events.request-get
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.db :refer [app-state]]
            [reagent.cookies :as cookies]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]]
            )
  )


(defn request_get_handler [[ok? response]]
  (swap! app-state assoc :request
         (if ok?
           (mapv (fn [product] (:data product)) response)
           []
           )
         )
  )



(defn request_get []
  (let []
    (http/ajax-request-with-headers
     {:uri (api_uri_maker "request-get")
      :method :post
      :params {}
      :handler request_get_handler
      :format (ajax/json-request-format)
      :response-format (ajax/json-response-format {:keywords? true})})
    )
  )