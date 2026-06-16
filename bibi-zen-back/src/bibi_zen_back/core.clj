(ns bibi-zen-back.core
  (:require
   [org.httpkit.server :as server]
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.json :refer [wrap-json-params]]
   [compojure.core :refer :all]
   [clojure.data.json :as json]
   [clj-http.client :as http]
   [ring.middleware.defaults :refer :all]
   [compojure.route :as route] 
   [clojure.string :as s]
   [clojure.core.async :as async]
   [cheshire.core :as che]
   [clojure.data.codec.base64 :as base64] 
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [clojure.pprint :as pprint]
   
   [bibi-zen-back.datamodule :as dm]
   [bibi-zen-back.app-data :as ad]
   [bibi-zen-back.tg-auth :as auth]
   )
  (:import [java.util.zip GZIPInputStream]
    [java.io ByteArrayInputStream ByteArrayOutputStream]
    (javax.crypto Mac)
    (javax.crypto.spec SecretKeySpec))
  (:gen-class)
  )

(def api_keys (ad/app_data :api_keys))

(defn send-telegram-message
  ([chat-id text]
   (send-telegram-message chat-id text "Markdown" {}))

  ([chat-id text parse-mode]
   (send-telegram-message chat-id text parse-mode {}))

  ([chat-id text parse-mode reply-markup]
   (let [bot-token (:bot-token api_keys)
         url (str "https://api.telegram.org/bot" bot-token "/sendMessage")
         params {:chat_id (str chat-id)
                 :text text
                 :parse_mode parse-mode
                 :reply_markup (che/generate-string reply-markup)
                 }
         ]

     (try
       (let [response (http/post url
                                 {:form-params params
                                  :content-type :json
                                  :socket-timeout 5000
                                  :conn-timeout 5000
                                  :as :json})
             body (:body response)]

         (if (:ok body)
           (do
             (println "✅ Сообщение отправлено в chat-id:" chat-id)
             {:success true :result body})
           (do
             (println "❌ Ошибка Telegram API для" chat-id ":" (:description body))
             {:success false :error (:description body)})))

       (catch Exception e
         (println "❌ Сетевая ошибка для" chat-id ":" (.getMessage e))
         {:success false :error (.getMessage e)})))))

(defn mp4-file? [file]
  (try
    (with-open [in (io/input-stream file)]
      (let [buf (byte-array 12)
            read-bytes (.read in buf)]
        (and (>= read-bytes 12)
             (= "ftyp" (String. buf 4 4 "US-ASCII")))))
    (catch Exception _
      false)))

(defn send-telegram-animation
  ([chat-id file-path]
   (send-telegram-animation chat-id file-path nil nil))
  ([chat-id file-path caption parse-mode]
   (let [bot-token (:bot-token api_keys)
         url (str "https://api.telegram.org/bot" bot-token "/sendAnimation")
         file (io/file file-path)
         file-name (.getName file)
         multipart (cond-> [{:name "chat_id" :content (str chat-id)}
                            {:part-name "animation"
                             :name file-name
                             :content file
                             :mime-type "image/gif"}]
                     caption (conj {:name "caption" :content caption})
                     parse-mode (conj {:name "parse_mode" :content parse-mode}))]
     (if-not (.exists file)
       (do
         (println "❌ GIF файл не найден:" (.getPath file))
         {:success false :error "GIF file not found"})
       (try
         (let [response (http/post url
                                   {:multipart multipart
                                    :socket-timeout 5000
                                    :conn-timeout 5000
                                    :throw-exceptions false
                                    :as :json})
               body (:body response)]
           (if (:ok body)
             (do
               (println "✅ GIF отправлен в chat-id:" chat-id)
               {:success true :result body})
             (do
               (println "❌ Ошибка Telegram API (GIF) для" chat-id "| status:" (:status response) "| body:" body)
               {:success false :error (:description body)})))
         (catch Exception e
           (println "❌ Сетевая ошибка при отправке GIF для" chat-id ":" (.getMessage e))
           {:success false :error (.getMessage e)}))))))

(defn send-message-1 [chat-id]
  (send-telegram-message
   chat-id
   "*Привет, это BIBI ZEN!*\n\nМы предлагаем готовый каталог товаров внутри Телеграмма.\n\nБот пришлёт тебе 3 сообщения, в которых расскажет о каталоге.\n\nЗа 2 минуты ты:\n• Посмотришь демо-магазин\n• Увидишь, как добавлять товары\n• Решишь, хочешь ли себе такой"
   "Markdown"))

(defn send-message-2 [chat-id]
  (send-telegram-message
   chat-id
   "*ДЕМО–МАГАЗИН*\n\nПара быстрых рекомендаций, пока не приступили:\n\n• Пройди полный путь (от выбора товара до оформления заказа)\n• Оцени, насколько это быстро и удобно\n\n*Ты можешь:*\n✅ Листать категории, фильтры, ленту товаров\n✅ Кликать по товарам, добавлять в корзину\n✅ Зайти в корзину, просматривать её\n✅ Перейти к оформлению заказа\n✅ Попробовать вызов оплаты (Режим оплаты тестовый, деньги не списываются)"
   "Markdown"
   {:inline_keyboard [[{:text "🔗 Открыть демо-магазин"
                        :url "https://t.me/bibi_zen_demo_store_bot?startapp"}]]}))



(defn send-message-3 [chat-id]
  (send-telegram-animation
   chat-id
   "admingif.mp4"
   "*Как это администрировать? Легко!*\n\nК каждому каталогу в комплекте идет приложение-админка, через которую можно:\n• Менять товары в каталоге\n• Настраивать приложение\n• Подключать оплату\nИ многое другое\n\nВот пример работы в админке: добавление нового товара в каталог.\n\nПрямо из Telegram либо из браузера, без программистов и сложных манипуляций:"
   "Markdown"))



(defn send-message-4 [chat-id]
  (send-telegram-message
   chat-id
   "🎁 У нас для тебя подарок\n\nСейчас мы дарим абсолютно бесплатный полный доступ на 14 дней! Никаких оплат, сразу сможешь попробовать в боевом режиме.\n\n✅ *Каталог уже готов* – не нужно ждать разработку\n✅ *Он живой* – клиенты видят всё, что есть в наличии\n✅ *Управление* – внутри Telegram\n\nЕсли это то, что ты искал – оставляй заявку!"
   "Markdown"
   {:inline_keyboard [[{:text "🎯 Попробовать бесплатно"
                        :url "https://t.me/bibi_zen_bot?startapp"}]]}))



(defn send-trigger-message [chat-id]
  (send-telegram-message
   chat-id
   "⏰ <b>Пока не оставил заявку?</b>\n\nВозможно, ты думаешь:\n\n❓ <b>«А вдруг сложно?»</b> → Нет, настраиваем за 7 минут (замеряли)\n❓ <b>«А вдруг дорого?»</b> → Стоимость – от 1490₽ в месяц (меньше, чем обед в кафе и СИЛЬНО меньше чем з/п менеджера для продаж через личку)\n❓ <b>«А вдруг не подойдёт?»</b> → Демо ты уже видел – он работает!\n\n💡 Пробный период бесплатный – на пробу уйдёт только <tg-emoji emoji-id=\"5447584416274595624\">1</tg-emoji><tg-emoji emoji-id=\"5447616284931933807\">5</tg-emoji> минут твоего времени!\n⚙️ Мы поможем всё настроить и проверить"
   "HTML"
   {:inline_keyboard [[{:text "✅ Готов попробовать"
                        :url "https://t.me/bibi_zen_bot?startapp"}]]}))

(defn start-message-sequence [chat-id]
  
  (send-message-1 chat-id)

  (future
    (Thread/sleep 5000)
    (send-message-2 chat-id))

  (future
    (Thread/sleep 25000)
    (send-message-3 chat-id))

  (future
    (Thread/sleep 35000)
    (send-message-4 chat-id))

  (future
    (Thread/sleep 720000)
    (if true
      (send-trigger-message chat-id)
      )
    )
  )


(defn telegram-webhook [req]
  (let [body (:params req)
        message (:message body)]

    (when (and message (:text message) (.startsWith (:text message) "/start"))
      (let [chat-id (get-in message [:chat :id])
            from-user (:from message)
            text (:text message)
            start-param (when (> (count text) 7)
                          (s/trim (subs text 7)))
            user-params (if (and start-param (not (s/blank? start-param)))
                          {:telegram_user_id (:id from-user)
                           :users_attributes [{:attribute_name "startbot"
                                               :attribute_value start-param
                                               :update_existing false}]}
                          {:telegram_user_id (:id from-user)})]
        (println "[Bot] Start param:" start-param)
        (dm/db_query_sender "" dm/users_users_add_sql user-params)
        (println "🔄 Запуск последовательности сообщений для chat-id:" chat-id)
        (start-message-sequence chat-id))))

  {:status 200 :body "OK"})




(defn users_get_init [req]
  (println "users_get_init")
  (let [params (:params req)
        telegram_user_id (:telegram_user_id params)
        db_res (dm/db_query_sender "" dm/users_users_get_init_sql {:telegram_user_id telegram_user_id})]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)}
    )
  )

(defn policies_get [req]
  (println "policies_get")
  (let [params (:params req)
        telegram_user_id (:telegram_user_id params)
        db_res (dm/db_query_sender "" dm/users_policies_get_sql {:telegram_user_id telegram_user_id})]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)}
    )
  )

(defn policies_all_actual_get [req]
  (println "policies_all_actual_get")
  (let [db_res (dm/db_query_sender "" dm/users_all_actual_policies_get_sql {})
        ]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)}
    )
  )

(defn policies_set [req]
  (println "policies_set")
  (let [params (:params req)
        telegram_user_id (:telegram_user_id params)
        users_attributes (:users_attributes params)
        db_res (dm/db_query_sender "" dm/users_users_attributes_add_sql{:telegram_user_id telegram_user_id :users_attributes users_attributes})]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)}
    )
  )


(defn request_add [req]
  (println "request_add")
  (let [params (:params req)
        telegram_user_id (:telegram_user_id params)
        form_data (:form_data params)
        db_res (dm/db_query_sender "" dm/users_users_form_add_sql {:telegram_user_id telegram_user_id :form_data form_data})]
    (println db_res)
    (when telegram_user_id
      (send-telegram-message
       "311278679"
       (str "📩 Новая заявка от "
            "<a href=\"tg://user?id=" telegram_user_id "\">"
            "пользователя " telegram_user_id
            "</a>.")
       "HTML")
      )
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)
     }
    )
  )


(defn request_get [req]
  (println "request_get")
  (let [params (:params req)
        telegram_user_id (:telegram_user_id params)
        db_res (dm/db_query_sender "" dm/users_users_form_get_sql {:telegram_user_id telegram_user_id})]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)
     }
    )
  )

(defn stores_get [req]
  (println "stores_get")
  (let [params (:params req)
        telegram_user_id (:telegram_user_id params)
        db_res (dm/db_query_sender "" dm/stores_stores_get_sql {:telegram_user_id telegram_user_id})]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)}
    )
  )

(defn tariffs_get [_req]
  (println "tariffs_get")
  (let [db_res (dm/db_query_sender "" dm/tariffs_tariffs_get_sql {})]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)}
    )
  )


(defn tariffs_stores_options_connect_price_get [req]
  (println "tariffs_stores_options_connect_price_get")
  (let [params    (:params req)
        store_uid (:store_uid params)
        option_ids (:option_ids params)
        db_res    (dm/db_query_sender "" dm/tariffs_stores_options_connect_price_get_sql
                                      {:store_uid store_uid :option_ids option_ids})]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)}))

(defn tariffs_stores_options_add [req]
  (println "tariffs_stores_options_add")
  (let [params     (:params req)
        store_uid  (:store_uid params)
        option_ids (:option_ids params)
        db_res     (dm/db_query_sender "" dm/tariffs_stores_options_add_sql
                                       {:store_uid store_uid :option_ids option_ids})]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)}))

(defn tariffs_stores_options_enabled_set [req]
  (println "tariffs_stores_options_enabled_set")
  (let [params    (:params req)
        store_uid (:store_uid params)
        options   (:options params)
        db_res    (dm/db_query_sender "" dm/tariffs_stores_options_enabled_set_sql
                                      {:store_uid store_uid :options options})]
    (println db_res)
    {:status  200
     :headers {"Content-Type" "text/json"}
     :body    (json/write-str db_res)}))

(defroutes api-routes
  (POST  "/users-get-init"                              []  users_get_init)
  (POST  "/policies-get"                                []  policies_get)
  (POST  "/policies-all-actual-get"                     []  policies_all_actual_get)
  (POST  "/policies-set"                                []  policies_set)
  (POST  "/request-add"                                 []  request_add)
  (POST  "/request-get"                                 []  request_get)
  (POST  "/stores-get"                                  []  stores_get)
  (POST  "/tariffs-get"                                 []  tariffs_get)
  (POST  "/tariffs-stores-options-connect-price-get"    []  tariffs_stores_options_connect_price_get)
  (POST  "/tariffs-stores-options-add"                  []  tariffs_stores_options_add)
  (POST  "/tariffs-stores-options-enabled-set"          []  tariffs_stores_options_enabled_set)
  )

(defroutes webhook-routes
  (POST  "/telegram"                   []  telegram-webhook)
  )

(defroutes app-routes
  (context "/api" []
    (-> api-routes
        (auth/wrap-telegram-auth (:bot-token api_keys))))

  (context "/webhook" [] 
    webhook-routes 
    )

  (route/not-found "There is no route you are looking for"))






(def app (-> app-routes
             (wrap-cors :access-control-allow-origin [#".*"]
                        :access-control-allow-methods [:get :post :put :delete :options]
                        :access-control-allow-headers ["Content-Type" "X-Telegram-InitData"])
             wrap-keyword-params
             wrap-params
             wrap-json-params 
             )
  )

(defn -main [& args]
  (server/run-server app {:port (ad/app_data :port)
                          :max-body 1000000000
                          :max-ws 1000000000
                          :max-line 1000000000
                          :timeout 3600000}) 
  (println (str "Server started on port " (ad/app_data :port))))
