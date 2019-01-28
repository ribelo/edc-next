(ns edc-next.db.events
  (:require [re-frame.core :as rf]
            [taoensso.encore :as e]
            [taoensso.timbre :as timbre]
            [oops.core :refer [ocall]]
            [edc-next.transit :as t]
            [edc-next.db.core :as db.core]))


(rf/reg-event-fx
  :db/init-state
  (fn [_ _]
    {:db db.core/default-db}))


(rf/reg-event-fx
  :db/load-async-storage!
  (fn [_ _]
    (-> (db.core/load-async-storage)
        (ocall "then" #(rf/dispatch-sync [:db/load-async-storage!.success %]))
        (ocall "catch" #(rf/dispatch-sync [:db/load-async-storage!.failure %])))
    nil))


(rf/reg-event-fx
  :db/load-async-storage!.success
  (fn [{db :db} [_ storage]]
    {:db (e/nested-merge db (or (t/<-json storage) {}))}))


(rf/reg-event-fx
  :db/load-async-storage!.failure
  (fn [{db :db} [_ storage]]
    (timbre/error :db/load-async-storage!.failure)))


(rf/reg-event-fx
  :db/save-async-storage!
  (fn [{db :db} _]
    (db.core/save-async-storage db)
    nil))


(rf/reg-event-fx
  :db/clear-async-storage!
  (fn [_ _]
    (db.core/clear-async-storage)
    nil))