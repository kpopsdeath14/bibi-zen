(ns bibi-zen-front.router
  (:import goog.history.Html5History
           goog.Uri)
  (:require
   [goog.events :as e]
   [bibi-zen-front.db :refer [app-state]]
   [secretary.core :as secretary :refer-macros [defroute]]
   [goog.history.EventType :as EventType]

   [reagent.core :as reagent]
   )
  )


(set! *warn-on-infer* true)

(defn hook-browser-navigation! []
  (doto (Html5History.)
    (e/listen
     EventType/NAVIGATE
     (fn [^js/Foo.Bar event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))



(defn routes []

  (secretary/set-config! :prefix "#")

  (defroute "/" []
    (swap! app-state assoc :page :main)
    )

  (defroute "/main" []
    (swap! app-state assoc :page :main)
    ) 
  
  (defroute "/shop" []
    (swap! app-state assoc :page :shop)
    )
  
  (defroute "/request" []
    (swap! app-state assoc :page :request)
    ) 
  
  (defroute "/information" []
    (swap! app-state assoc :page :information)
    ) 

  (hook-browser-navigation!)
  )
