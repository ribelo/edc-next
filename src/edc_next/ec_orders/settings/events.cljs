(ns edc-next.ec-orders.settings.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.db.core :refer [->async-storage]]))


(rf/reg-event-db
  :ec-orders.settings/show-settings-dialog
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:ec-orders :settings :_show-settings-dialog?] val db)
      (sp/transform [:ec-orders :settings :_show-settings-dialog?] not db))))


(rf/reg-event-db
  :ec-orders.settings/set-pace-period
  [->async-storage]
  (fn [db [_ val]]
    (sp/setval [:ec-orders :creator :pace-period] val db)))


(rf/reg-event-db
  :ec-orders.settings/toggle-product-card-elem
  [->async-storage]
  (fn [db [_ key]]
    (sp/transform [:ec-orders :settings :product-card key] not db)))


(rf/reg-event-db
  :ec-orders.settings/set-card-columns
  [->async-storage]
  (fn [db [_ val]]
    (sp/setval [:ec-orders :settings :card-columns] (max 0 val) db)))
