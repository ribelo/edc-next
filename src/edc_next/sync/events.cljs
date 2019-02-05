(ns edc-next.sync.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(rf/reg-event-fx
  :sync/document->collector
  (fn [{db :db} [_ {:keys [products] :as document}]]
    {:ws/send {:event  :sync/document->collector
               :params products}}))


(rf/reg-event-fx
  :sync/document->ftp
  (fn [{db :db} [_ {:keys [products] :as document}]]
    {:ws/send {:event  :sync/document->ftp
               :params (vals products)}}))                  ;TODO!!


(rf/reg-event-fx
  :sync/ftp->collector
  (fn [{db :db} [_ file-name]]
    {:ws/send {:event  :sync/ftp->collector
               :params file-name}}))
