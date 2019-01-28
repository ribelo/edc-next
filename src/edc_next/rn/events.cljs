(ns edc-next.rn.events
  (:require [re-frame.core :as rf]
            [oops.core :refer [ocall ocall+]]
            [edc-next.rn.core :as rn]))


;(ocall+ rn/back-handler :addEventListener "hardwareBackPress"
;        (fn [] (println "sex")))


(rf/reg-event-fx
  :rn/back-handler!
  (fn [{db :db} [_ val]]
    (println "sex")))



(rf/reg-event-fx
  :rn/vibrate!
  (fn [{db :db} [_ pattern]]
    (rn/vibrate pattern)))


(rf/reg-event-fx
  :rn/vibrate!
  (fn [{db :db} [_ pattern]]
    (rn/vibrate pattern)))


(rf/reg-event-fx
  :rn/keyboard-dismiss
  (fn [_ _]
    (ocall rn/keyboard "dismiss")))
