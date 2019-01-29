(ns edc-next.orders.settings.subs
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]))


(rf/reg-sub
  :orders.settings/show-settings-dialog?
  (fn [db _]
    (sp/select-one [:orders :settings :_show-settings-dialog?] db)))


(rf/reg-sub
  :orders.settings/pace-period
  (fn [db _]
    (sp/select-one [:orders :settings :pace-period] db)))


(rf/reg-sub
  :orders.settings/card-columns
  (fn [db _]
    (sp/select-one [:orders :settings :card-columns] db)))


(rf/reg-sub
  :orders.settings/product-card-info
  (fn [db _]
    (sp/select-one [:orders :settings :product-card] db)))


(rf/reg-sub
  :orders.settings/product-card.show?
  (fn [db [_ k]]
    (sp/select-one [:orders :settings :product-card k] db)))
