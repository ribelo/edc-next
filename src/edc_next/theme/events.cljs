(ns edc-next.theme.events
  (:require [re-frame.core :as rf]
            [edc-next.rnp.core :as rnp]
            [edc-next.theme.db :as theme.db]))


(rf/reg-event-db
  :theme/init-state
  (fn [db _]
    (merge db theme.db/state)))


(rf/reg-event-db
  :theme/set
  (fn [db [_ style]]
    (assoc db :theme (js->clj (rnp/theme style) :keywordize-keys true))))


