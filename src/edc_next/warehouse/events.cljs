(ns edc-next.warehouse.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.timbre :as timbre]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.warehouse.db :as warehouse.db]))


(rf/reg-event-fx
  :warehouse/get-products
  (fn [{db :db} _]
    {:db      (sp/setval [:warehouse :_data-loading?] true db)
     :ws/send {:event      :data/market-report
               :on-success [:warehouse/get-products.success]
               :on-failure [:warehouse/get-products.failure]}}))


(rf/reg-event-fx
  :warehouse/get-products.success
  (fn [{db :db} [_ {:keys [data]}]]
    {:db (->> db
              (sp/setval [:warehouse :_products/by-ean] data)
              (sp/setval [:warehouse :_data-loading?] false))
     :dispatch [:orders/reset-view]}))


(rf/reg-event-fx
  :warehouse/get-products.failure
  (fn [{db :db} [_ {:keys [data]}]]
    (timbre/error :warehouse/get-products.failure)))