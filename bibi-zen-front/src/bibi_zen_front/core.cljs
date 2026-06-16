(ns bibi-zen-front.core
  (:require
   ["antd" :as antd]
   [reagent.core :as reagent]
   [reagent.dom :as d]

   [bibi-zen-front.db :refer [app-state]]
   [bibi-zen-front.router :refer [routes]]
   [bibi-zen-front.viewes :refer [current-page]]
   [bibi-zen-front.pages.policies.policies :refer [policies]]

   [bibi-zen-front.events.users-get-init :refer [users_get_init]]
   [bibi-zen-front.events.policies-get :refer [policies_get]]
   [bibi-zen-front.events.all-actual-policies-get :refer [all_actual_policies_get]]
   [bibi-zen-front.events.request-get :refer [request_get]]
   [bibi-zen-front.events.stores-get :refer [stores_get]]
   [bibi-zen-front.events.tariffs-get :refer [tariffs_get]]

   [clojure.string :as str]
   [clojure.edn :as edn]
   )
  )


(defn is-mobile? []
  (let [user-agent (.-userAgent (.-navigator js/window))
        mobile-regex #"(?i)android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini"]
    (boolean (re-find mobile-regex user-agent))))


(defn- bibi-zen-bot-url [state]
  (let [config (:config state)
        direct-url (:bibi_zen_bot_url config)
        username (:bibi_zen_bot_username config)]
    (cond
      (and (string? direct-url) (not (str/blank? direct-url))) direct-url
      (and (string? username) (not (str/blank? username))) (str "https://t.me/" username)
      :else "https://t.me/tr1j3Tz")))


(defn page_template []
  (let [Layout antd/Layout
        Header (.-Header Layout)
        Content (.-Content Layout)
        ConfigProvider antd/ConfigProvider
        Typography antd/Typography
        Text (.-Text Typography)

        web-app (.-WebApp js/Telegram)

        platform (when web-app (.-platform web-app))
        is-tg-mobile? (contains? #{"ios" "android"} platform)
        is-mobile-device? (is-mobile?)
        is-mobile (or is-tg-mobile? is-mobile-device?)

        header-height (if is-mobile 144 64)]
    (fn []
      [:> ConfigProvider {:theme {:components {}}
                          :wave {:disabled true}}
       [:> Layout {:style {:overflow-y "hidden"}}

        [:> Header {:style {:background-color "#0A5137"
                            :height header-height
                            :display "flex"
                            :align-items "flex-end"
                            :justify-content "center"}}
         [:div {:style {:color "white"
                        :font-size "40px"
                        :font-family "'cabrito', sans-serif"
                        :font-weight 200
                        :letter-spacing "1px"
                        :word-spacing "5px"}}
          "BIBI ZEN"]]

        [:> Content
         {:style
          {:display "flex"
           :flex-direction "column"
           :min-height (str "calc(100vh - " header-height "px)")
           :padding "25px 10%"
           :box-sizing "border-box"
           :overflow-y "auto"}}
         [:<>
          [policies]
          [current-page]

          [:div {:style {:text-align "center"
                         :font-size "12px"
                         :position "sticky"
                         :margin-top "auto"
                         :padding-top 50
                         :color "#0A5137"}}
           [:div {:onClick (fn []
                             (let [p (.-pathname js/window.location)
                                   base (str "/" (first (remove empty? (.split p "/"))) "/")]
                               (set! (.-href js/window.location) (str base "#/information"))))
                  :style {:margin-bottom 5}}
            "Страница информации"]]]]]])))


(defn mount-root []
  (let [main_button js/Telegram.WebApp.MainButton
        back-button js/Telegram.WebApp.BackButton

        texting? (reagent/cursor app-state [:texting?])
        ]

    (add-watch app-state :page_listener (fn [key atom old-state new-state]
                                          (if (= :main (:page new-state))
                                            (.hide back-button)
                                            (.show back-button)
                                            )
                                          )
               )


  (routes)
  (d/render [page_template] (.getElementById js/document "app")))
  )




(defn load-config []
  (-> (js/fetch "config.edn")
      (.then (fn [response] (.text response)))
      (.then (fn [text] (edn/read-string text)))
      (.catch (fn [error] (js/console.error "Error loading config:" error)))
      (.then (fn [config]
               (swap! app-state assoc :config config)
               (users_get_init)
               (policies_get)
               (all_actual_policies_get)
               (stores_get)
               (request_get)
               (tariffs_get)
               (mount-root)
               )
             )
      )
  )




(defn ^:export init! []
  (let [
        web-app (.-WebApp js/Telegram)
        user (.. js/Telegram -WebApp -initDataUnsafe -user)
        start_param (.. js/Telegram -WebApp -initDataUnsafe -start_param)
        back-button (.-BackButton web-app)
        main_button js/Telegram.WebApp.MainButton
        ]

    (if (is-mobile?)
     (.requestFullscreen web-app)
      )
    (.disableVerticalSwipes web-app)

    (.lockOrientation web-app)
    (load-config)

    (.setBottomBarColor web-app "#FFFFFF")

    (.onClick back-button (fn []
                            (swap! app-state assoc :page :main)
                            (let [p (.-pathname js/window.location)
                                  base (str "/" (first (remove empty? (.split p "/"))) "/")]
                              (set! (.-href js/window.location) (str base "#/main")))
                            )
              )
    )
  )
