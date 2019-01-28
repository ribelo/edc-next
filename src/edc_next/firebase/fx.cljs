(ns edc-next.firebase.fx
  (:require [re-frame.core :as rf]
            [com.rpl.specter :as sp]
            [edc-next.firebase.core :as fb]))


(rf/reg-fx :firestore/get
  (fn [{:keys [path on-success on-failure]}]
    (fb/get-doc path
                :on-success on-success
                :on-failure on-failure)))


(rf/reg-fx :firestore/set
  (fn [{:keys [path doc on-success on-failure]}]
    (fb/set-doc path doc
                :on-success on-success
                :on-failure on-failure)))


(rf/reg-fx :firestore/update
  (fn [{:keys [path doc on-success on-failure]}]
    (fb/update-doc path doc
                   :on-success on-success
                   :on-failure on-failure)))


(rf/reg-fx :firestore/add
  (fn [{:keys [path doc on-success on-failure]}]
    (fb/add-doc path doc
                :on-success on-success
                :on-failure on-failure)))


(rf/reg-fx :firestore/delete
  (fn [{:keys [path on-success on-failure]}]
    (fb/del-doc path
                :on-success on-success
                :on-failure on-failure)))


(rf/reg-fx :firestore/subscribe
  (fn [{:keys [path write-to]}]
    (fb/on-doc-snapshot! path write-to)))


(rf/reg-fx :firestore/subscribe-coll
  (fn [{:keys [path dispatch]}]
    (fb/on-coll-snapshot! path dispatch)))


(rf/reg-fx :firestore/unsubscribe
  (fn [{:keys [path dispatch]}]
    (when dispatch (rf/dispatch dispatch))
    (fb/unsubscribe path)))


(rf/reg-fx :firestore/unsubscribe-coll
  (fn [{:keys [path dispatch]}]
    (when dispatch (rf/dispatch dispatch))
    (fb/unsubscribe-coll path)))
