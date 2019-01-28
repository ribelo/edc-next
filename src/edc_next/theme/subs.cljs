(ns edc-next.theme.subs
  (:require [re-frame.core :as rf]))


(rf/reg-sub
  :theme/get
  (fn [db [_ & attrs]]
    (if attrs
      (get-in db (into [:theme] attrs))
      (get db :theme))))


(rf/reg-sub
  :theme/get.js
  :<- [:theme/get]
  (fn [theme [_ & attrs]]
    (clj->js (get-in theme attrs))))