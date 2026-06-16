(ns bibi-zen-front.events.options-price-get
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]]))

(defn options_price_get [store-uid option-ids callback]
  (http/ajax-request-with-headers
   {:uri             (api_uri_maker "tariffs-stores-options-connect-price-get")
    :method          :post
    :params          {:store_uid  store-uid
                      :option_ids option-ids}
    :handler         (fn [[ok? response]]
                       (when ok? (callback response))
                       )
    :format          (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})}))
