(ns bibi-zen-front.db
  (:require
   [reagent.core :as r]
   )
  )

(defonce app-state (r/atom {:page :main
                            :policies_menu_open? false
                            :actual_policies []
                            :stores []
                            :current-store nil
                            :user_role nil
                            :user_status nil
                            :app_state nil
                            :tariffs []
                            :tariff-options []}
                           ))
