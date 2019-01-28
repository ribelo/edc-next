(ns edc-next.camera.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [oops.core :refer [oget]]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.expo.core :refer [expo]]
            [edc-next.db.core :refer [->async-storage]]
            [edc-next.camera.core :as camera]))


(rf/reg-event-db
  :camera/toogle-fab
  (fn [db _]
    (sp/transform [:camera :_fab-open?] not db)))


(rf/reg-event-db
  :camera/switch-flash-mode
  [->async-storage]
  (fn [db _]
    (let [mode (get-in db [:camera :_flash-mode])
          mode* (condp = mode
                  camera/flash-mode-off camera/flash-mode-torch
                  camera/flash-mode-torch camera/flash-mode-off
                  camera/flash-mode-torch)]
      (sp/setval [:camera :_flash-mode] mode* db))))


(rf/reg-event-db
  :camera/show-preview
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:camera :_show-preview?] val db)
      (sp/transform [:camera :_show-preview?] not db))))


(rf/reg-event-db
  :camera/inc-zoom
  [->async-storage]
  (fn [db _]
    (let [zoom (get-in db [:camera :zoom])
          zoom* (min 1.0 (+ zoom 0.1))]
      (assoc-in db [:camera :zoom] zoom*))))


(rf/reg-event-db
  :camera/dec-zoom
  [->async-storage]
  (fn [db _]
    (let [zoom (get-in db [:camera :zoom])
          zoom* (max 0.0 (- zoom 0.1))]
      (sp/setval [:camera :zoom] zoom* db))))
