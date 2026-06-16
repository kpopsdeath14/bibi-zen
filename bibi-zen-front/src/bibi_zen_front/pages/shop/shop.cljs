(ns bibi-zen-front.pages.shop.shop
  (:require
   ["antd" :as antd]
   [bibi-zen-front.db :refer [app-state]]
   [bibi-zen-front.events.options-price-get :refer [options_price_get]]
   [bibi-zen-front.events.options-add :refer [options_add]]
   [bibi-zen-front.events.options-enabled-set :refer [options_enabled_set]]
   [bibi-zen-front.events.stores-get :refer [stores_get]]
   [bibi-zen-front.events.tariffs-get :refer [tariffs_get]]
   [reagent.core :as reagent]))

(def green "#0A5137")

(defn get-tariff-attr [tariff attr-name]
  (->> (:tariffs_attributes tariff)
       (filter #(= (:attribute_name %) attr-name))
       first
       :attribute_value))

;; ── option rows ───────────────────────────────────────────────────────────────

(defn purchased-option-row [opt active? on-toggle]
  [:div {:style {:padding "12px 0" :border-bottom "1px solid rgba(10,81,55,0.12)"}}
   [:div {:style {:display "flex" :align-items "center" :justify-content "space-between" :gap "8px"}}
    [:div
     [:div {:style {:font-size "15px" :font-weight 500 :color green}}
      (:option_name opt)]
     (when (:option_price opt)
       [:div {:style {:font-size "11px" :font-weight 400 :color "#888" :margin-top "2px"}}
        (str (:option_price opt) " ₽ / мес.")])]
    [:> antd/Switch {:checked  active?
                     :onChange on-toggle
                     :style    {:background-color (if active? green "#ccc")}}]]
   (when (:option_description opt)
     [:div {:style {:font-size "12px" :font-weight 300 :color green :opacity 0.75 :margin-top "4px"}}
      (:option_description opt)])])

(defn unpurchased-option-row [opt connect-price period-price selected? on-click]
  [:div {:on-click on-click
         :style    {:padding "12px 0" :border-bottom "1px solid rgba(10,81,55,0.12)"
                    :cursor "pointer" :user-select "none"
                    :background (if selected? "rgba(10,81,55,0.05)" "transparent")
                    :margin "0 -18px" :padding-left "18px" :padding-right "18px"}}
   [:div {:style {:display "flex" :align-items "flex-start" :justify-content "space-between" :gap "8px"}}
    [:div
     [:div {:style {:font-size "15px" :font-weight 500 :color green}}
      (:option_name opt)]
     (when period-price
       [:div {:style {:font-size "11px" :font-weight 400 :color "#888" :margin-top "2px"}}
        (str period-price " ₽ / мес.")])]
    [:span {:style {:font-size "13px" :font-weight 700 :color green :white-space "nowrap" :flex-shrink 0}}
     (str "подключить за " (or connect-price "…") " ₽")]]
   (when (:option_description opt)
     [:div {:style {:font-size "12px" :font-weight 300 :color green :opacity 0.75 :margin-top "4px"}}
      (:option_description opt)])])

;; ── page ─────────────────────────────────────────────────────────────────────

(defn shop_page []
  (let [stores           (reagent/cursor app-state [:stores])
        current-store    (reagent/cursor app-state [:current-store])
        tariffs          (reagent/cursor app-state [:tariffs])
        all-opts         (reagent/cursor app-state [:tariff-options])
        cart             (reagent/atom #{})
        toggle-states    (reagent/atom {})
        prices           (reagent/atom {})
        prices-requested (reagent/atom false)
        main-btn         (.-MainButton (.-WebApp js/Telegram))]

    (.setParams main-btn #js {:color "#0A5137" :text_color "#FFFFFF"})

    (reagent/create-class
     {:component-will-unmount
      (fn [] (.hide main-btn))

      :reagent-render
      (fn []
        (let [shop-data      (or @current-store (first @stores))
              shop-name      (or (:store_name shop-data) (:name shop-data) "Магазин")
              current-tariff (:current_tariff shop-data)
              tariff-name    (or (:tariff_name current-tariff) "—")
              planned-until  (:planned_until current-tariff)
              planned-date   (when (string? planned-until) (js/Date. planned-until))
              planned-date-text (if (and planned-date (not (js/isNaN (.getTime planned-date))))
                                  (.toLocaleDateString planned-date "ru-RU")
                                  (or planned-until "—"))

              tariff-id      (or (:tariff_id current-tariff) (:id current-tariff))
              tariff         (first (filter #(= (str (:tariff_id %)) (str tariff-id)) @tariffs))
              tariff-options (sort-by :order_number (or (seq (:options tariff)) @all-opts []))

              store-options  (or (:options shop-data) [])
              purchased-ids  (set (map :option_id store-options))
              purchased-map  (into {} (map (fn [o] [(:option_id o) o]) store-options))

              store-uid      (:store_uid shop-data)
              unpurchased    (remove #(contains? purchased-ids (:option_id %)) tariff-options)
              option-ids     (mapv :option_id unpurchased)

              ;; Следующий платёж = цена тарифа + сумма включённых опций
              tariff-price    (js/parseInt (or (get-tariff-attr tariff "price") "0") 10)
              active-opts-sum (->> store-options
                                   (filter #(not (false? (:is_enabled %))))
                                   (reduce #(+ %1 (or (:option_price %2) 0)) 0))
              next-payment    (+ tariff-price active-opts-sum)

              cart-total     (->> tariff-options
                                  (filter #(contains? @cart (:option_id %)))
                                  (reduce #(+ %1 (or (:connect_price (get @prices (:option_id %2)))
                                                      (:option_price %2) 0)) 0))]

          ;; Запрашиваем цены когда данные готовы
          (when (and store-uid (seq option-ids) (not @prices-requested))
            (reset! prices-requested true)
            (options_price_get store-uid option-ids
                               (fn [response]
                                 (reset! prices
                                         (into {} (map (fn [r]
                                                         (let [d (:data r)]
                                                           [(:option_id d) d]))
                                                       (if (sequential? response) response [response])))))))

          ;; MainButton
          (if (seq @cart)
            (do (.setText main-btn (str "Оплатить " cart-total " ₽"))
                (.onClick main-btn
                          (fn []
                            (options_add store-uid (vec @cart)
                                         (fn [_]
                                           (reset! cart #{})
                                           (reset! prices {})
                                           (reset! prices-requested false)
                                           (stores_get)
                                           (tariffs_get)))))
                (.show main-btn))
            (.hide main-btn))

          [:> antd/Flex {:vertical true :gap 24}

           [:div {:style {:font-size "20px" :color "#333" :font-weight 500}}
            shop-name]

           ;; Тариф + следующий платёж
           [:div {:style {:font-size "16px" :color green :font-weight 400
                          :line-height "1.6" :display "flex" :flex-direction "column" :gap "2px"}}
            [:div (str "Текущий тариф: «" tariff-name "»")]
            [:div {:style {:display "flex" :align-items "baseline" :gap "6px" :flex-wrap "wrap"}}
             "Следующий платёж: "
             [:span {:style {:display "inline-block" :padding "2px 8px" :border-radius "8px"
                             :background-color "#E6F4EA" :color green :font-weight 600}}
              planned-date-text]
             [:span {:style {:font-weight 600}}
              (str next-payment " ₽")]]]

           ;; Опции
           (when (seq tariff-options)
             [:div {:style {:overflow "hidden"}}
              [:style "@keyframes shimmer{0%{background-position:-200% 0}100%{background-position:200% 0}}
                       .sk{background:linear-gradient(90deg,#eee 25%,#ddd 50%,#eee 75%);background-size:200% 100%;animation:shimmer 1.4s infinite;border-radius:6px}"]
              [:div {:style {:font-size "16px" :font-weight 500 :color green :margin-bottom "4px"}}
               "Опции"]
              (if (and @prices-requested (empty? @prices) (seq option-ids))
                (for [opt tariff-options]
                  ^{:key (:option_id opt)}
                  [:div {:style {:padding "12px 0" :border-bottom "1px solid rgba(10,81,55,0.12)"}}
                   [:div {:style {:display "flex" :justify-content "space-between" :align-items "center" :gap "8px"}}
                    [:div {:class "sk" :style {:height "15px" :width "40%"}}]
                    [:div {:style {:display "flex" :flex-direction "column" :gap "5px" :align-items "flex-end"}}
                     [:div {:class "sk" :style {:height "14px" :width "110px"}}]
                     [:div {:class "sk" :style {:height "11px" :width "75px"}}]]]])
                (for [opt tariff-options]
                  (let [id         (:option_id opt)
                        purchased? (contains? purchased-ids id)]
                    ^{:key id}
                    (if purchased?
                      (let [store-opt (get purchased-map id)
                            active?   (if (contains? @toggle-states id)
                                        (get @toggle-states id)
                                        (not (false? (:is_enabled store-opt))))]
                        [purchased-option-row opt active?
                         (fn [val]
                           (swap! toggle-states assoc id val)
                           (options_enabled_set store-uid [{:option_id id :is_enabled val}]
                                                (fn [_]
                                                  (stores_get)
                                                  (tariffs_get))))])
                      [unpurchased-option-row opt
                       (or (:connect_price (get @prices id)) (:option_price opt))
                       (or (:option_price (get @prices id)) (:option_price opt))
                       (contains? @cart id)
                       (fn [] (swap! cart (fn [s] (if (contains? s id) (disj s id) (conj s id)))))]))))])

           [:> antd/Flex {:vertical true :gap 12}
            [:div {:style {:height "calc((100vw / 15) * 2)" :min-height "44px"
                           :background-color "white" :border-radius "calc((100vw / 15) / 2)"
                           :border "2px solid #0A5137" :font-size "20px" :font-weight "500"
                           :color green :display "flex" :align-items "center" :justify-content "center"}}
             "Приложение каталога"]
            [:div {:style {:height "calc((100vw / 15) * 2)" :min-height "44px"
                           :background-color "white" :border-radius "calc((100vw / 15) / 2)"
                           :border "2px solid #0A5137" :font-size "20px" :font-weight "500"
                           :color green :display "flex" :align-items "center" :justify-content "center"}}
             "Приложение админки"]]]))})))
