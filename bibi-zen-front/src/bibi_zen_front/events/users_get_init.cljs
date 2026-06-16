(ns bibi-zen-front.events.users-get-init
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.db :refer [app-state]]
            [reagent.cookies :as cookies]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]] 
            )
  )

(defn users_get_init_handler [[ok? response]]
  (let [main_button js/Telegram.WebApp.MainButton
        info (:users_get_init (first response))]
    (swap! app-state assoc
           :user_role (:user_role info)
           :user_status (:user_status info)
           :app_state (:app_state info))
    (if (and (= "normis" (:user_status info)) (= "technical_work" (:app_state info)))
      (do
        (swap! app-state assoc :production false)
        (.hide main_button)
        )
      )
    )
  )



(defn users_get_init []
  (let []
    (http/ajax-request-with-headers
     {:uri (api_uri_maker "users-get-init")
      :method :post
      :params {}
      :handler users_get_init_handler
      :format (ajax/json-request-format)
      :response-format (ajax/json-response-format {:keywords? true})})))
