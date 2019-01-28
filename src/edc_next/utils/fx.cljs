(ns edc-next.utils.fx
  (:require [re-frame.core :as rf]
            [re-frame.router :as router]
            [re-frame.db :refer [app-db]]
            [taoensso.timbre :as timbre]))


(rf/reg-fx :dispatch-sync
           (fn [value]
             (if-not (vector? value)
               (timbre/error "re-frame: ignoring bad :dispatch value. Expected a vector, but got:" value)
               (router/dispatch-sync value))))


(rf/reg-fx :dispatch-sync-n
           (fn [value]
             (if-not (sequential? value)
               (timbre/error "re-frame: ignoring bad :dispatch-n value. Expected a collection but got:" value))
             (doseq [event value] (router/dispatch-sync event))))

