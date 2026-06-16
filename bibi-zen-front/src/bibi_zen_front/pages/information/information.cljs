(ns bibi-zen-front.pages.information.information
  (:require
   ["antd" :as antd]
   [bibi-zen-front.db :refer [app-state]]
   ["@ant-design/icons" :as icons]
   ["react-photo-view" :as photo_review]
   [reagent.core :as reagent :refer [as-element]]
   [clojure.string :as str]
   )
  )

(def policy-labels
  {"BIBIZEN_cookies" "Согласие - Cookies."
   "BIBIZEN_personal_data" "Политика обработки ПДн для пользователей веб сайтов."
   "BIBIZEN_personal_data_agreement" "Согласие на обработку ПНд."
   "BIBIZEN_public_oferta" "Договор-оферта платформы в Telegram."})

(def policy-order
  ["BIBIZEN_cookies"
   "BIBIZEN_personal_data"
   "BIBIZEN_personal_data_agreement"
   "BIBIZEN_public_oferta"])

(defn policy-link [policy]
  (or (:policy_title policy)
      (:policy_content policy)
      (:policy_link policy)
      (:policy_url policy)
      (:link policy)
      (:url policy)))


(defn information_page []
  (let [web-app (.-WebApp js/Telegram)
        actual-policies (reagent/cursor app-state [:actual_policies])]
    (fn []
      (let [policies-by-name (into {}
                                   (map (fn [policy] [(:policy_name policy) policy])
                                        @actual-policies))]
        [:div
         [:div {:style {:margin "0 10px 20px"
                        :padding "16px 20px"
                        :border-radius "12px"
                        :background-color "#f7f7f7"}}
          [:div {:style {:font-size "18px"
                         :font-weight 600
                         :margin-bottom 12
                         :color "#0A5137"}}
           "Документы"]
          (for [policy-name policy-order
                :let [label (get policy-labels policy-name)
                      policy (get policies-by-name policy-name)
                      link (policy-link policy)
                      clickable? (and label link)]]
            ^{:key policy-name}
            [:div {:style {:font-size "15px"
                           :line-height "1.4"
                           :margin-bottom 14
                           :color (if clickable? "#0A5137" "#444")
                           :text-decoration (when clickable? "underline")
                           :cursor (if clickable? "pointer" "default")}
                   :onClick (when clickable?
                              (fn []
                                (if web-app
                                  (.openLink web-app link)
                                  (set! (.-href (.-location js/window)) link)
                                  )
                                )
                              )
                   }
             label])]
         ]
         )
         )
         )
         )
