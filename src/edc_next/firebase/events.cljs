(ns edc-next.firebase.events
  (:require [re-frame.core :as rf]
            [oops.core :refer [oget ocall]]
            [com.rpl.specter :as sp]
            [edc-next.firebase.core :as fb]
            [edc-next.firebase.db :refer [config]]))


(rf/reg-event-fx
  :firebase/init
  (fn [_ _]
    (ocall fb/firebase "initializeApp" (clj->js config))
    nil))


(rf/reg-event-fx
  :firestore/subscribe
  (fn [_ [_ params]]
    {:firestore/subscribe params}))


(rf/reg-event-fx
  :firestore/unsubscribe
  (fn [_ [_ params]]
    {:firestore/unsubscribe params}))


(rf/reg-event-fx
  :firestore/unsubscribe-coll
  (fn [_ [_ params]]
    {:firestore/unsubscribe-coll params}))


