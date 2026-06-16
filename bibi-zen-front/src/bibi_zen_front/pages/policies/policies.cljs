(ns bibi-zen-front.pages.policies.policies
  (:require
   ["antd" :as antd]
   [bibi-zen-front.db :refer [app-state]]
   ["@ant-design/icons" :as icons]
   ["react-photo-view" :as photo_review]
   [reagent.core :as reagent :refer [as-element]]
   [clojure.string :as str]
   [bibi-zen-front.events.policies-set :refer [policies_set]]
   )
  )


(defn policies []
  (let [Drawer antd/Drawer
        Button antd/Button
        policies_menu_open? (reagent/cursor app-state [:policies_menu_open?])
        policies (reagent/cursor app-state [:policies])
        web-app (.-WebApp js/Telegram)
        ]
    
    (fn []
      [:> Drawer {:title (as-element [:div {:style {:font-size "20px" 
                                                    :font-weight 600
                                                    :color "#0A5137"
                                                    :text-align "center"}
                                            }
                                      "Настройки конфиденциальности"])
                  :placement "bottom"
                  :open @policies_menu_open?
                  :height "auto"
                  :style {:border-radius "24px 24px 0 0"
                          :max-height "80vh"}
                  :bodyStyle {:padding "24px 20px 32px"}
                  :closable false
                  }
       
       [:div {:style {:display "flex"
                      :flex-direction "column"
                      :height "100%"}}
        

        [:div {:style {:flex-grow 1
                       :white-space "pre-line"
                       :font-size "16px"
                       :line-height "1.5"
                       :color "#333"
                       :text-align "left"
                       :padding "0 8px"}
               }
         
         "Продолжая использовать приложение вы даете:"
         
         [:div {:style {:margin "12px 0"}}
          [:div {:style {:display "inline-flex"
                         :align-items "center"
                         :gap "4px"}}
           "Согласие на использование cookies и аналогичных технологий для улучшения работы сервиса"]
          ]

         [:div {:style {:display "flex"
                        :justify-content "center"
                        :margin-bottom "16px"}}
          [:div {:style {:width "120px"
                         :height "4px"
                         :background-color "#e8e8e8"
                         :border-radius "2px"}}]]
         
         [:div {:style {:margin "16px 0"}}
          (let [personal_data_agreement (first (filter #(= (:policy_name %) "BIBIZEN_personal_data_agreement")
                                                       @policies))]
            (if personal_data_agreement
              [:span {:style {:color "#0A5137"
                              :text-decoration "underline"
                              :font-weight 500
                              :cursor "pointer"}
                      :onClick (fn []
                                 (let [p (.-pathname js/window.location)
                                       base (str "/" (first (remove empty? (.split p "/"))) "/")]
                                   (set! (.-href js/window.location) (str base "#/information"))
                                   )

                                 ;
                                 )
                      }
               "Согласие"]
              "Согласие")
            )
          
          " на обработку своих персональных данных" 
          "в соответствии с "
          
          (let [personal_data (first (filter #(= (:policy_name %) "BIBIZEN_personal_data")
                                             @policies))]
            (if personal_data
              [:span {:style {:color "#0A5137"
                              :text-decoration "underline"
                              :font-weight 500
                              :cursor "pointer"}
                      :onClick (fn []
                                 (let [p (.-pathname js/window.location)
                                       base (str "/" (first (remove empty? (.split p "/"))) "/")]
                                   (set! (.-href js/window.location) (str base "#/information")))
                                 )
                      }
               "Политикой конфиденциальности"]
              "Политикой конфиденциальности")
            )
          
          [:span {:style {:color "#0A5137"
                          :font-weight 500}
                  }
           " сервисов BIBI-ZEN"]
          ]
         ]
        
        [:div {:style {:display "flex"
                       :flex-direction "column"
                       :align-items "center"
                       :gap "12px"
                       :margin-top "24px"}}
         
         [:> Button
          {:style {:background "linear-gradient(135deg, 
                         #083b29 0%, 
                         #0A5137 25%, 
                         #0c5e42 50%, 
                         #0A5137 75%, 
                         #083b29 100%)"
                   :background-size "400% 400%"
                   :width "100%"
                   :max-width "320px"
                   :height "52px"
                   :font-weight 600
                   :font-size "18px"
                   :border-radius "12px"
                   :border "none"
                   :box-shadow "0 4px 20px rgba(10, 81, 55, 0.4)"
                   :animation "gradientFlow 4s ease-in-out infinite"
                   :position "relative"
                   :overflow "hidden"
                   :color "#fff"
                   :transition "transform 0.5s cubic-bezier(0.18, 0.89, 0.32, 1.28),
                                box-shadow 0.5s ease-out,
                                background 0.5s ease-out,
                                background-size 0.5s ease-out"}
           :type "primary"
           :onMouseEnter (fn [e]
                           (let [el e.target]
                             ;; Быстрая остановка анимации
                             (set! (.. el -style -animation) "none")
                             
                             ;; Мгновенное визуальное изменение (без ожидания)
                             (set! (.. el -style -transform) "translateY(-6px) scale(1.03)")
                             
                             ;; Градиент меняется с анимацией 0.25s
                             (set! (.. el -style -background)
                                   "linear-gradient(135deg, 
                                  #0d7452 0%, 
                                  #0f815c 25%, 
                                  #11a06b 50%, 
                                  #0f815c 75%, 
                                  #0d7452 100%)")
                             
                             ;; Тень меняется с анимацией 0.2s
                             (set! (.. el -style -boxShadow)
                                   "0 8px 30px rgba(13, 116, 82, 0.6)")
                             
                             ;; Размер градиента меняется с анимацией 0.3s
                             (set! (.. el -style -backgroundSize) "200% 200%")
                             
                             ;; Без setTimeout - сразу запускаем движение
                             (set! (.. el -style -backgroundPosition) "100% 100%")))
           :onMouseLeave (fn [e]
                           (let [el e.target]
                             ;; Возвращаем исходные значения
                             (set! (.. el -style -transform) "translateY(0) scale(1)")
                             (set! (.. el -style -background)
                                   "linear-gradient(135deg, 
                                  #083b29 0%, 
                                  #0A5137 25%, 
                                  #0c5e42 50%, 
                                  #0A5137 75%, 
                                  #083b29 100%)")
                             (set! (.. el -style -boxShadow)
                                   "0 4px 20px rgba(10, 81, 55, 0.4)")
                             (set! (.. el -style -backgroundSize) "400% 400%")
                             
                             ;; Возвращаем анимацию
                             (set! (.. el -style -animation)
                                   "gradientFlow 4s ease-in-out infinite")))
           :onClick (fn []
                      (let [current-date (.toISOString (js/Date.))]
                        (policies_set (mapv (fn [policy]
                                              {:attribute_name (str (:policy_name policy) "_ACCEPTED_DATE")
                                               :attribute_value current-date})
                                            @policies))))}
          "Соглашаюсь"]
         
         ]
        ]
       ]
         )
         )
         )
        