(ns bibi-zen-back.datamodule
  (:require
   [bibi-zen-back.app-data :as ad]
   )
  )

(import 'org.postgresql.util.PGobject)
(require '[next.jdbc.prepare :as prepare])
(require '[next.jdbc.result-set :as rs])
(require '[next.jdbc :as jdbc])
(require '[next.jdbc.sql :as sql])
(require '[jsonista.core :as json_])
(require '[clojure.java.io :as io])
(require '[clojure.string :as str])



(require '[clojure.edn :as edn])

(def mapper (json_/object-mapper {:decode-key-fn keyword}))
(def ->json json_/write-value-as-string)
(def <-json #(json_/read-value % mapper))


(defn ->pgobject
  "Transforms Clojure data to a PGobject that contains the data as
  JSON. PGObject type defaults to `jsonb` but can be changed via
  metadata key `:pgtype`"
  [x]
  (let [pgtype (or (:pgtype (meta x)) "jsonb")]
    (doto (PGobject.)
      (.setType pgtype)
      (.setValue (->json x)))))

(defn <-pgobject
  "Transform PGobject containing `json` or `jsonb` value to Clojure
  data."
  [^org.postgresql.util.PGobject v]
  (let [type  (.getType v)
        value (.getValue v)]
    (if (#{"jsonb" "json"} type)
      (with-meta (<-json value) {:pgtype type})
      value)))

(import  '[java.sql PreparedStatement])

(set! *warn-on-reflection* true)



(extend-protocol prepare/SettableParameter
  clojure.lang.IPersistentMap
  (set-parameter [m ^PreparedStatement s i]
    (.setObject s i (->pgobject m)))

  clojure.lang.IPersistentVector
  (set-parameter [v ^PreparedStatement s i]
    (.setObject s i (->pgobject v))))



(extend-protocol rs/ReadableColumn
  org.postgresql.util.PGobject
  (read-column-by-label [^org.postgresql.util.PGobject v _]
    (<-pgobject v))
  (read-column-by-index [^org.postgresql.util.PGobject v _2 _3]
    (<-pgobject v)))



(def users_users_add_sql                    "SELECT        \"users\".users_add(_p := ?);")
(def users_users_get_init_sql               "SELECT        \"users\".users_get_init(_p := ?);")
(def users_policies_get_sql                 "SELECT * FROM \"users\".policies_get(_p := ?);")
(def users_all_actual_policies_get_sql      "SELECT * FROM \"users\".all_actual_policies_get();")
(def users_users_attributes_add_sql         "CALL            users.users_attributes_add(_p := ?);")
(def users_users_form_add_sql               "CALL            users.users_form_add(_p := ?);")
(def users_users_form_get_sql               "SELECT * FROM   users.users_form_get(_p := ?);")
(def stores_stores_get_sql                  "SELECT * FROM   stores.stores_get(_p := ?);")
(def tariffs_tariffs_get_sql                "SELECT * FROM   tariffs.tariffs_get();")
(def tariffs_stores_options_connect_price_get_sql "SELECT * FROM   tariffs.tariffs_stores_options_connect_price_get(_p := ?);")
(def tariffs_stores_options_add_sql              "CALL            tariffs.tariffs_stores_options_add(_p := ?);")
(def tariffs_stores_options_enabled_set_sql      "CALL            tariffs.tariffs_stores_options_enabled_set(_p := ?);")


(def ui_product_get_list_sql                "SELECT * FROM   ui.product_get_list(_p := ?);")
(def ui_product_get_single_unit             "SELECT * FROM   ui.product_get_single_unit(_p := ?);")
(def product_get_one_per_row_sql            "SELECT * FROM   product.product_get_one_per_row(_p := ?);")
(def config_sysconfig_unit_get              "SELECT * FROM \"config\".sysconfig_unit_get(_p := ?);")
(def product_product_attribute_get_filter   "SELECT * FROM   product.product_attribute_get_filter();")
(def user_cart_set_sql                      "CALL          \"user\".cart_set(_p := ?);")
(def user_cart_get_sql                      "SELECT * FROM \"user\".cart_get(_p := ?);")
(def user_cart_get_cummary_sql              "SELECT        \"user\".cart_get_summary(_p := ?);")
(def user_user_attribute_get                "SELECT * FROM \"user\".user_attribute_get(_p := ?);")
(def order_user_order_get_sql               "SELECT * FROM \"order\".user_order_get(_p := ?);")
(def product_storage_moysklad_stock_upd_sql "CALL          \"product\".storage_moysklad_stock_upd(_p := ?);")
(def order_payments_add_sql                 "SELECT        \"order\".payments_add(_p := ?);")
(def order_payments_upd_sql                 "CALL          \"order\".payments_upd(_p := ?);")
(def user_user_attribute_add_sql            "CALL          \"user\".user_attribute_add(_p := ?);")
(def user_cart_set_complete_sql             "SELECT        \"user\".cart_set_complete(_p := ?);")
(def banner_get_sql                         "SELECT * FROM   ui.banners_get(_p := ?);")
(def exchange_history_add_sql               "SELECT * FROM   xtrnl.exchange_history_add(_p := ?);")
(def exchange_history_upd_sql               "CALL            xtrnl.exchange_history_upd(_p := ?);")
(def history_get_cdek_sql                   "SELECT * FROM   xtrnl.exchange_history_get_cdek(_p := ?);")
(def history_get_crm_sql                    "SELECT          xtrnl.exchange_history_get_crm(_p := ?);")
(def user_delivery_cost_get_sql             "SELECT        \"user\".delivery_cost_get(_p := ?);")
(def user_policies_get_sql                  "SELECT * FROM \"user\".policies_get(_p := ?);")



(def config_sysconfig_catalog_get_sql       "SELECT * FROM \"config\".sysconfig_catalog_get(_p := ?);")


(def mypg-db (ad/app_data :db_data))


(def db_con (jdbc/get-datasource mypg-db))


(defn db_query_sender
  [query_inf temp params]
  (let [first_part_of_query_string (subs temp 0 (str/index-of temp "("))
        splited_query_string (str/split first_part_of_query_string #" ")
        method_name (splited_query_string (- (count splited_query_string) 1))]
    (if (= 1 (- (str/index-of temp ")") (str/index-of temp "(")))
      (let [db_ans (sql/query db_con [temp])]
        db_ans)
      (let [db_ans (sql/query db_con [temp params])]
        db_ans))))


(defn db_proxy
  [query_string params]
  (try (def db_ans (sql/query db_con [query_string params]))
       (catch Exception e (str "caught exception: " (.getMessage e))))

  (if (nil? (:error_message db_ans))
    db_ans
    (println (str "exception message: " (:error_message db_ans)))))
