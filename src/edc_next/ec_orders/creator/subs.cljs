(ns edc-next.ec-orders.creator.subs
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(rf/reg-sub
  :ec-orders.creator/min-pace
  (fn [db _]
    (sp/select-one [:ec-orders :creator :min-pace] db)))


(rf/reg-sub
  :ec-orders.creator/min-margin
  (fn [db _]
    (sp/select-one [:ec-orders :creator :min-margin] db)))


(rf/reg-sub
  :ec-orders.creator/only-below-minimum?
  (fn [db _]
    (sp/select-one [:ec-orders :creator :only-below-minimum?] db)))


(rf/reg-sub
  :ec-orders.creator/only-cheaper-than-cg?
  (fn [db _]
    (sp/select-one [:ec-orders :creator :only-cheaper-than-cg?] db)))


(rf/reg-sub
  :ec-orders.creator/show-make-order-dialog?
  (fn [db _]
    (sp/select-one [:ec-orders :creator :_show-make-order-dialog?] db)))


(rf/reg-sub
  :ec-orders.creator/selected-categories
  (fn [db _]
    (sp/select-one [:ec-orders :creator :selected-categories] db)))


(rf/reg-sub
  :ec-orders.creator/category-selected?
  (fn [db [_ id]]
    (sp/select-one [:ec-orders :creator :selected-categories id] db)))
