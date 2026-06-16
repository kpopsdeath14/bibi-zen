(ns bibi-zen-front.events.options-enabled-set
  (:require [ajax.core :as ajax]
            [bibi-zen-front.http-client :as http]
            [bibi-zen-front.apiurimaker :refer [api_uri_maker]]))

(defn options_enabled_set [store-uid options callback]
  ;; options — вектор [{:option_id 1 :is_enabled true} ...]
  (http/ajax-request-with-headers
   {:uri             (api_uri_maker "tariffs-stores-options-enabled-set")
    :method          :post
    :params          {:store_uid store-uid
                      :options   options}
    :handler         (fn [[ok? response]]
                       (when (and ok? callback) (callback response)))
    :format          (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})}))
