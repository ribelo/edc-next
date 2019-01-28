(ns edc-next.camera.subs
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(rf/reg-sub
  :camera/fab-open?
  (fn [db _]
    (sp/select-one [:camera :_fab-open?] db)))


(rf/reg-sub
  :camera/flash-mode
  (fn [db _]
    (sp/select-one [:camera :_flash-mode] db)))


(rf/reg-sub
  :camera/zoom
  (fn [db _]
    (sp/select-one [:camera :zoom] db)))


(rf/reg-sub
  :camera/barcode-types
  (fn [db _]
    (sp/select-one [:camera :barcode-types] db)))


(rf/reg-sub
  :camera/barcode-detected
  (fn [db _]
    (sp/select-one [:camera :_barcode-detected] db)))


(rf/reg-sub
  :camera/show-preview?
  (fn [db _]
    (sp/select-one [:camera :_show-preview?] db)))
