(ns bibi-zen-front.viewes
  (:require
   [reagent.core :as reagent]
   [bibi-zen-front.db :refer [app-state]]
   [reagent.ratom :as ratom]
   [bibi-zen-front.pages.main.main :refer [main_page]]
   [bibi-zen-front.pages.request.request :refer [request_page]]
   [bibi-zen-front.pages.shop.shop :refer [shop_page]] 
   [bibi-zen-front.pages.information.information :refer [information_page]] 
   )
  )

(defmulti current-page #(@app-state :page))


(defmethod current-page :main []
  [main_page]
  )

(defmethod current-page :request []
  [request_page]
  )

(defmethod current-page :shop []
  [shop_page]
  )

(defmethod current-page :shop []
  [shop_page]
  )

(defmethod current-page :information []
  [information_page]
  )
