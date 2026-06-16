(ns bibi-zen-front.events.tariffs-get
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.db :refer [app-state]]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]]))

(defn tariffs_get_handler [[ok? response]]
  (when ok?
    (let [tariffs     (->> response
                           (mapv :data)
                           (sort-by :order_number)
                           (mapv (fn [t]
                                   (update t :options
                                           #(vec (sort-by :order_number %))))))
          all-options (->> tariffs
                           (mapcat :options)
                           (group-by :option_id)
                           vals
                           (mapv first))]
      (swap! app-state assoc
             :tariffs tariffs
             :tariff-options all-options))))

(defn tariffs_get []
  (http/ajax-request-with-headers
   {:uri             (api_uri_maker "tariffs-get")
    :method          :post
    :params          {}
    :handler         tariffs_get_handler
    :format          (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})}))
