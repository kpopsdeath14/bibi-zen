(ns bibi-zen-front.events.request-add
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            ["antd" :as antd]
            [bibi-zen-front.db :refer [app-state]]
            [reagent.cookies :as cookies]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]]
            [bibi-zen-front.events.request-get :refer [request_get]]
            )
  )

(def message (.-message antd))

(defn request_add_handler [[ok? response]]
  (let [web-app (.-WebApp js/Telegram)]

    (request_get)

    (.info message
           (clj->js
            {:content "Вас скоро перебросит в чат с администратором..."
             :style {:whiteSpace "pre-line"
                     :fontSize "14px"}
             :duration 3}))

    (js/setTimeout
     (fn []
       (let [username "tr1j3Tz"
             message-text "Привет, я губка боб! У меня вопрос."
             telegram-url (str "https://t.me/" username "?text="message-text)
             ]
         (.openTelegramLink web-app telegram-url)
         )
       )
     3000)
    )
    )



(defn request_add [form_data]
  (let []
    (http/ajax-request-with-headers
     {:uri (api_uri_maker "request-add")
      :method :post
      :params {:form_data form_data}
      :handler request_add_handler
      :format (ajax/json-request-format)
      :response-format (ajax/json-response-format {:keywords? true})})))