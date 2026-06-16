(ns bibi-zen-front.pages.main.main
  (:require
   ["antd" :as antd]
   [bibi-zen-front.db :refer [app-state]]
   [bibi-zen-front.utils :refer [store-tariff-active?]]
   ["@ant-design/icons" :as icons]
   ["react-photo-view" :as photo_review]
   [reagent.core :as reagent :refer [as-element]]
   [clojure.string :as str]
   )
  )


(defn main_page []
  (let [Flex antd/Flex
        Button antd/Button
        PlusOutlined icons/PlusOutlined

        stores (reagent/cursor app-state [:stores])
        request (reagent/cursor app-state [:request])
        ]

    (fn []
      [:> Flex {:vertical true
                :gap 24
                :style {}
                }
       
       [:div {:style {:font-size "20px"
                      :color "#0A5137"
                      :font-weight 500
                      :margin-bottom "8px"}}
        "Ваши магазины:"]
       
       (if (seq @stores)
         [:> Flex {:vertical true
                   :gap 12}
          (doall
           (map-indexed
            (fn [idx shop]
              (let [shop-name (or (:store_name shop) (:name shop) "Магазин")
                    shop-key (or (:store_uid shop)
                                 (:store_id shop)
                                 (:id shop)
                                 (:user_uid shop)
                                 (str "shop-" idx))
                    tariff-active? (store-tariff-active? shop)]
                ^{:key shop-key}
                [:div {:style {:position "relative"}
                       :onClick (fn []
                                  (swap! app-state assoc :current-store shop :page :shop)
                                  (let [p (.-pathname js/window.location)
                                        base (str "/" (first (remove empty? (.split p "/"))) "/")]
                                    (set! (.-href js/window.location) (str base "#/shop"))))}
                 [:div {:style {:height "calc((100vw / 15) * 2)"
                                :min-height "44px"
                                :background-color "white"
                                :border-radius "calc((100vw / 15) / 2)"
                                :border (if tariff-active? "2px solid #0A5137" "2px solid #FF4D4F")
                                :font-size "20px"
                                :font-weight "500"
                                :color "#0A5137"
                                :display "flex"
                                :align-items "center"
                                :justify-content "center"}}
                  shop-name]
                 (when (not tariff-active?)
                   [:div {:style {:position "absolute"
                                  :top "-6px"
                                  :right "-6px"
                                  :width "24px"
                                  :height "24px"
                                  :background-color "#FF4D4F"
                                  :border-radius "50%"
                                  :display "flex"
                                  :align-items "center"
                                  :justify-content "center"
                                  :color "white"
                                  :font-size "14px"
                                  :font-weight "700"
                                  :line-height "1"}}
                    "!"])]))
            @stores))]
         
         (if (empty? @request)
           [:> Flex {:vertical true
                     :gap 24
                     :style {}}
            
            [:div {:style {:font-size "20px"
                           :font-weight 100
                           :color "#0A5137"
                           :margin-bottom "8px"
                           :text-align "center"}}
             "Пока здесь пусто"]
            
            [:> Button {:type "primary"
                        :size "large"
                        :icon (as-element [:> PlusOutlined])
                        :block true
                        :style {:height "calc((100vw / 15) * 2)"
                                :min-height "44px"
                                :background-color "#0A5137"
                                :border-radius "calc((100vw / 15) / 2)"
                                :font-weight "500"
                                :font-size "20px"}
                        :onClick (fn []
                                   (let [p (.-pathname js/window.location)
                                         base (str "/" (first (remove empty? (.split p "/"))) "/")]
                                     (set! (.-href js/window.location) (str base "#/request"))))}
             "Создать новый магазин"]]
           
           [:div {:style {:font-size "20px"
                          :font-weight 100
                          :color "#0A5137"
                          :margin-bottom "8px"
                          :text-align "center"
                          :white-space "pre-line"}}
            "Вы оставили заявку.
                  Скоро с вами свяжется
                  наш менеджер."]))
       
       ]
      )
    ) 
  )
