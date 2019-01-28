(ns edc-next.rnrf.events
  (:require [re-frame.core :as rf]
            [taoensso.encore :as e]
            [oops.core :refer [oget+]]
            [com.rpl.specter :as sp]
            [edc-next.rnrf.core :as rnrf]))


(rf/reg-event-fx
  :rnrf/navigate!
  (fn [_ [_ k]]
    (rnrf/action! k)))


(rf/reg-event-fx
  :rnrf/pop!
  (fn [_ _]
    (println :rnrf/pop!)
    (rnrf/action! :pop)))


(rf/reg-event-fx
  :rnrf/push!
  (fn [_ [_ k]]
    (println :rnrf/push! k)
    (rnrf/action! :push k)))


(rf/reg-event-fx
  :rnrf/replace!
  (fn [_ [_ k]]
    (println :rnrf/replace! k)
    (rnrf/action! :replace k)))


(rf/reg-event-fx
  :rnrf/open-drawer!
  (fn [_ _]
    (rnrf/action! :drawerOpen)))


(rf/reg-event-fx
  :rnrf/close-drawer!
  (fn [_ _]
    (rnrf/action! :drawerClose)))


(rf/reg-event-fx
  :rnrf/refresh-scene
  (fn [{db :db} _]
    (let [scene (oget+ rnrf/actions "currentScene")
          scene* (e/str-replace scene #"_" "")]
      {:db (sp/setval [:rnrf :current-scene] scene* db)})))
