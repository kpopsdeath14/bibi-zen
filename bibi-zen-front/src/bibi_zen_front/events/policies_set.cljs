(ns bibi-zen-front.events.policies-set
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.db :refer [app-state]]
            [reagent.cookies :as cookies]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]]
            [bibi-zen-front.events.policies-get :refer [policies_get]]
            )
  )


(defn policies_set_handler [[ok? response]]
  (policies_get)
  (swap! app-state assoc :policies_menu_open? false)
  )



(defn policies_set [users_attributes]
  (let []
    (http/ajax-request-with-headers
     {:uri (api_uri_maker "policies-set")
      :method :post
      :params {:users_attributes users_attributes}
      :handler policies_set_handler
      :format (ajax/json-request-format)
      :response-format (ajax/json-response-format {:keywords? true})}
     )
    )
  )