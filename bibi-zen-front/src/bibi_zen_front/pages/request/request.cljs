(ns bibi-zen-front.pages.request.request
  (:require
   ["antd" :as antd]
   [bibi-zen-front.db :refer [app-state]]
   [reagent.core :as r]
   [bibi-zen-front.events.request-add :refer [request_add]]
   [bibi-zen-front.events.tariffs-get :refer [tariffs_get]]))

(def green "#0A5137")
(def light-green "rgba(10,81,55,0.07)")

(def input-style
  {:height           "calc((100vw / 15) * 2)"
   :min-height       "44px"
   :background-color "white"
   :border-radius    "calc((100vw / 15) / 2)"
   :border           (str "2px solid " green)
   :font-size        "20px"
   :font-weight      "300"
   :color            green})

;; ── helpers ──────────────────────────────────────────────────────────────────

(defn get-attr [tariff attr-name]
  (->> (:tariffs_attributes tariff)
       (filter #(= (:attribute_name %) attr-name))
       first
       :attribute_value))

(defn format-period [p]
  (case p
    "30 days"  " / мес."
    "7 days"   " / 7 дней"
    "14 days"  " / 14 дней"
    "365 days" " / год"
    (when p (str " / " p))))

(defn format-price [price period]
  (let [n (js/parseInt price 10)]
    (str n "₽" (format-period period))))

(defn features-vec [opt]
  (let [f (:option_features opt)]
    (cond (nil? f) [] (string? f) [f] (sequential? f) (vec f) :else [f])))

;; ── option row (inside card) ─────────────────────────────────────────────────

(defn option-row [opt tariff-selected selected-options]
  (let [id         (:option_id opt)
        checked    (contains? @selected-options id)
        on-white   (not tariff-selected)
        box-border (if on-white green "white")
        box-bg     (if checked (if on-white green "white") "transparent")
        tick-color (if on-white "white" green)]
    [:div
     {:on-click (fn [e]
                  (.stopPropagation e)
                  (swap! selected-options (if checked disj conj) id))
      :style {:padding     "10px 0"
              :cursor      "pointer"
              :user-select "none"}}

     ;; Name + price + checkbox on one line
     [:div {:style {:display "flex" :align-items "center" :justify-content "space-between" :gap "8px"}}
      [:div {:style {:font-size "15px" :font-weight 500}}
       (:option_name opt)]
      [:div {:style {:display "flex" :align-items "center" :gap "8px" :flex-shrink 0}}
       (when (:option_price opt)
         [:span {:style {:font-size "14px" :font-weight 600 :white-space "nowrap"}}
          (str "+" (:option_price opt) " ₽")])
       [:div {:style {:width           "22px"
                      :height          "22px"
                      :border-radius   "5px"
                      :border          (str "2px solid " box-border)
                      :background      box-bg
                      :display         "flex"
                      :align-items     "center"
                      :justify-content "center"
                      :flex-shrink     0}}
        (when checked
          [:span {:style {:color tick-color :font-size "13px" :font-weight 700 :line-height 1}}
           "✓"])]]]

     ;; Description + features full width below
     (when (:option_description opt)
       [:div {:style {:font-size "12px" :font-weight 300 :opacity 0.75 :margin-top "2px"}}
        (:option_description opt)])
     (when (seq (features-vec opt))
       [:div {:style {:margin-top "4px" :display "flex" :flex-direction "column" :gap "2px"}}
        (for [f (features-vec opt)]
          [:div {:key f :style {:font-size "12px" :font-weight 300 :opacity 0.7
                                :display "flex" :gap "4px"}}
           [:span "·"] [:span f]])])]))

;; ── tariff card ──────────────────────────────────────────────────────────────

(defn tariff-card [t selected-tariff selected-options]
  (let [id       (:tariff_id t)
        selected (= @selected-tariff id)
        price    (get-attr t "price")
        period   (get-attr t "tariffs_period")
        options  (:options t)]
    [:div
     {:on-click #(reset! selected-tariff id)
      :style    {:border          (str (if selected "3px" "2px") " solid " green)
                 :border-radius   "18px"
                 :padding         "18px 18px"
                 :background      (if selected light-green "white")
                 :color           green
                 :cursor          "pointer"
                 :box-sizing      "border-box"
                 :transition      "all 0.15s ease"
                 :user-select     "none"
                 :display         "flex"
                 :flex-direction  "column"}}

     ;; Name + radio dot
     [:div {:style {:display "flex" :justify-content "space-between" :align-items "flex-start"}}
      [:span {:style {:font-size "20px" :font-weight 700 :line-height 1.2}}
       (:tariff_name t)]
      [:div {:style {:width            "22px"
                     :height           "22px"
                     :border-radius    "50%"
                     :border           (str "2px solid " green)
                     :background       (if selected green "transparent")
                     :flex-shrink      0
                     :margin-top       "3px"
                     :display          "flex"
                     :align-items      "center"
                     :justify-content  "center"}}
       (when selected
         [:div {:style {:width "8px" :height "8px" :border-radius "50%" :background "white"}}])]]

     ;; Price
     (when price
       [:div {:style {:font-size "28px" :font-weight 700 :margin-top "14px" :line-height 1}}
        (format-price price period)])

     ;; Options divider + list
     (when (seq options)
       [:<>
        [:div {:style {:border-top    "1px solid rgba(10,81,55,0.2)"
                       :margin-top    "16px"
                       :margin-bottom "-2px"}}]
        [:div {:style {:display "flex" :flex-direction "column"}}
         (for [opt options]
           ^{:key (:option_id opt)}
           [option-row opt selected selected-options])]])]))

;; ── page ─────────────────────────────────────────────────────────────────────

(defn request_page []
  (tariffs_get)
  (let [Form             antd/Form
        FormItem         (.-Item Form)
        web-app          (.-WebApp js/Telegram)
        selected-tariff  (r/atom nil)
        selected-options (r/atom #{})]
    (fn []
      (let [tariffs (:tariffs @app-state)]
        [:div {:style {:display "flex" :flex-direction "column" :gap 80}}
         [:> antd/Flex {:vertical true :gap 12}
          [:div {:style {:font-size "20px" :color green :font-weight 500 :margin-bottom "8px"}}
           "Заполните заявку на каталог:"]

          [:> Form
           {:layout   "horizontal"
            :variant  "filled"
            :size     "large"
            :onFinish (fn [values]
                        (let [form_data (-> (js->clj values :keywordize-keys true)
                                            (assoc :tariff_id @selected-tariff)
                                            (assoc :options (vec @selected-options)))]
                          (request_add form_data)))}

           [:> FormItem {:name "name" :rules [{:required true :message "Пожалуйста, введите ваше имя"}]}
            [:> antd/Input {:placeholder "Имя" :style input-style :onChange (fn [])}]]

           [:> FormItem {:name "store_name" :rules [{:required true :message "Пожалуйста, введите название магазина"}]}
            [:> antd/Input {:placeholder "Название магазина" :style input-style :onChange (fn [])}]]

           [:> FormItem {:name "email"}
            [:> antd/Input {:placeholder "Email" :type "mail" :style input-style :onChange (fn [])}]]

           [:> FormItem {:name "phone" :rules [{:required true :message "Пожалуйста, введите номер телефона"}]}
            [:> antd/Input {:inputMode "tel" :placeholder "+7 (___) ___-__-__" :maxLength 18 :style input-style}]]

           [:> FormItem {:name "promo_code"}
            [:> antd/Input {:placeholder "ПРОМОКОД" :style input-style :onChange (fn [])}]]

           [:div {:style {:font-size "18px" :color green :font-weight 500 :margin-bottom "4px" :margin-top "8px"}}
            "Выберите тариф:"]

           (if (empty? tariffs)
             [:> antd/Spin {:size "default"}]
             [:div {:class "tariff-scroll"
                    :style {:display                    "flex"
                            :flex-direction             "row"
                            :flex-wrap                  "nowrap"
                            :gap                        "12px"
                            :overflow-x                 "auto"
                            :scroll-snap-type           "x mandatory"
                            :-webkit-overflow-scrolling "touch"
                            :padding                    "4px 0 12px 16px"
                            :margin-left                "-25px"
                            :margin-right               "-25px"
                            :scrollbar-width            "none"
                            :align-items                "stretch"}}
              (for [t tariffs]
                ^{:key (:tariff_id t)}
                [:div {:style {:scroll-snap-align "start"
                               :flex-shrink       0
                               :width             "82vw"
                               :max-width         "320px"}}
                 [tariff-card t selected-tariff selected-options]])])

           [:> FormItem
            [:> antd/Button
             {:type     "primary"
              :size     "large"
              :block    true
              :htmlType "submit"
              :style    {:height           "52px"
                         :background-color green
                         :border-radius    "calc((100vw / 15) / 2)"
                         :font-weight      "300"
                         :font-size        "24px"}}
             "Отправить"]]]]

         [:div {:style {:border        (str "3px solid " green)
                        :border-radius 10
                        :padding       15
                        :font-size     18
                        :text-align    "center"
                        :font-weight   100
                        :margin-top    20
                        :color         green}}
          "Нажимая кнопку \"отправить\", вы соглашаетесь с условиями "
          [:span {:style   {:color green :text-decoration "underline"}
                  :onClick (fn []
                             (.openLink web-app "https://bibi-zen.qq-pp.ru/policies/BIBI_ZEN_Договор_оферта_платформы_в_Telegram_v1_2_03_01_2026.pdf"))}
           "договора публичной оферты."]]]))))
