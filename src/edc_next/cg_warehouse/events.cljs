(ns edc-next.cg-warehouse.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [taoensso.timbre :as timbre]
            [com.rpl.specter :as sp]))


(rf/reg-event-fx
  :cg-warehouse/get-products
  (fn [{db :db} _]
    {:db      (sp/setval [:cg-warehouse :_data-loading?] true db)
     :ws/send {:event      :cg/cg-warehouse
               :on-success [:cg-warehouse/get-products.success]
               :on-failure [:cg-warehouse/get-products.failure]}}))


(rf/reg-event-fx
  :cg-warehouse/get-products.success
  (fn [{db :db} [_ {:keys [data]}]]
    {:db (->> db
              (sp/setval [:cg-warehouse :_products/by-ean] data)
              (sp/setval [:cg-warehouse :_data-loading?] false))}))


(rf/reg-event-fx
  :cg-warehouse/get-products.failure
  (fn [{db :db} [_ {:keys [data]}]]
    (timbre/error :cg-warehouse/get-products.failure)))