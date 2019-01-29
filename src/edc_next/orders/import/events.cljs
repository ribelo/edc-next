(ns edc-next.orders.import.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [taoensso.timbre :as timbre]
            [com.rpl.specter :as sp]))


(rf/reg-event-fx
  :orders.import/get-mm-file-list
  (fn [{db :db} _]
    {:ws/send {:event      :data/mm-file-list
               :on-success [:orders.import/get-mm-file-list.success]
               :on-failure [:orders.import/get-mm-file-list.failure]}}))


(rf/reg-event-fx
  :orders.import/get-mm-file-list.success
  (fn [{db :db} [_ {:keys [data]}]]
    (println data)
    {:db             (sp/setval [:orders :_mm-file-list] data db)
     :dispatch-later [{:dispatch [:orders.import/show-import-dialog true]
                       :ms       100}]}
    ))


(rf/reg-event-fx
  :orders.import/get-mm-file-list.failure
  (fn [{db :db} [_ {:keys [data]}]]
    (timbre/error :data/get-mm-file-list.failure)))


(rf/reg-event-db
  :orders.import/show-import-dialog
  (fn [db [_ val]]
    (println :orders.import/show-import-dialog)
    (if-not (nil? val)
      (sp/setval [:orders :_show-import-dialog?] val db)
      (sp/transform [:orders :_show-import-dialog?] not db))))
