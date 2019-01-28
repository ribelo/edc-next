(ns edc-next.rnrf.subs
  (:require [re-frame.core :as rf]))


(rf/reg-sub
  :rnrf/current-scene
  (fn [db _]
    (get-in db [:rnrf :current-scene])))
