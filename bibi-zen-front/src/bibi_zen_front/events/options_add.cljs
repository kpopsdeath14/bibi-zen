(ns bibi-zen-front.events.options-add
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]]))

(defn options_add [store-uid option-ids callback]
  (http/ajax-request-with-headers
   {:uri             (api_uri_maker "tariffs-stores-options-add")
    :method          :post
    :params          {:store_uid  store-uid
                      :option_ids option-ids}
    :handler         (fn [[ok? response]]
                       (when ok? (when callback (callback response))))
    :format          (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})}))
