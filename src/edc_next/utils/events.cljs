(ns edc-next.utils.events
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as rf]
            [taoensso.timbre :as timbre]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]))


(s/def ::event (s/and vector? #(keyword? (first %))))
(s/def ::events (s/* ::event))
(s/def ::event-type (s/or :event ::event :events ::events))
(s/def ::path vector?)
(s/def ::paths (s/* ::path))


(rf/reg-event-fx
  :trim
  (fn [_ [& evs]]
    {:dispatch-n (drop-last evs)}))


(rf/reg-event-db
  :sp/setval
  (fn [db [_ p v]]
    (sp/setval p v db)))


(rf/reg-event-db
  :sp/transform
  (fn [db [_ p f]]
    (sp/transform p f db)))


(rf/reg-event-db
  :write-to
  (fn [db [_ ks v]]
    (sp/setval ks v db)))


(rf/reg-event-db
  :write-key-to
  (fn [db [_ key ks v]]
    (sp/setval ks (get v key) db)))


(rf/reg-event-db
  :write-data
  (fn [db [_ ks v]]
    (sp/setval ks (:data v) db)))


(rf/reg-event-fx
  :write-to-later
  (fn [_ [_ ms k v]]
    {:dispatch-later [{:dispatch [:write-to k v]
                       :ms       ms}]}))


(rf/reg-event-fx
  :write-data-later
  (fn [_ [_ ms key k v]]
    {:dispatch-later [{:dispatch [:write-to k (get v key)]
                       :ms       ms}]}))


(rf/reg-event-db
  :clean-up
  (fn [db [_ ks]]
    (condp (first (s/conform ::event-type ks)) =
      :event (sp/setval ks sp/NONE db)
      :events (sp/setval (sp/multi-path ks) sp/NONE db))))


(rf/reg-event-fx
  :clean-up-later
  (fn [_ [_ ms paths]]
    {:dispatch-later [{:dispatch [:clean-up paths]
                       :ms       ms}]}))


(rf/reg-event-db
  :turn-on
  (fn [db [_ ks]]
    (condp (first (s/conform ::event-type ks)) =
      :event (sp/setval ks true db)
      :events (sp/setval (sp/multi-path ks) true db))))


(rf/reg-event-fx
  :turn-on-later
  (fn [_ [_ ms ks]]
    {:dispatch-later [{:dispatch [:turn-on ks]
                       :ms       ms}]}))


(rf/reg-event-db
  :turn-off
  (fn [db [_ ks]]
    (condp (first (s/conform ::event-type ks)) =
      :event (sp/setval ks false db)
      :events (sp/setval (sp/multi-path ks) false db))))


(rf/reg-event-fx
  :turn-off-later
  (fn [_ [_ ms ks]]
    {:dispatch-later [{:dispatch [:turn-off ks]
                       :ms       ms}]}))


(rf/reg-event-db
  :toggle
  (fn [db [_ ks]]
    (condp (first (s/conform ::event-type ks)) =
      :event (sp/transform ks not db)
      :events (sp/transform (sp/multi-path ks) not db))))


(rf/reg-event-fx
  :toggle-later
  (fn [_ [_ ms paths]]
    {:dispatch-later [{:dispatch [:toggle paths]
                       :ms       ms}]}))


(rf/reg-event-fx
  :do
  (fn [_ [_ & evs]]
    {:dispatch-n (filter #(s/valid? ::event %) evs)}))



(rf/reg-event-fx
  :do-later
  (fn [_ [_ ms & evs]]
    {:dispatch-later [{:dispatch (into [:do] evs)
                       :ms       ms}]}))


(rf/reg-event-db
  :conj
  (fn [db [_ ks v]]
    (sp/setval (conj ks sp/AFTER-ELEM) v db)))


(rf/reg-event-db
  :into
  (fn [db [_ ks v]]
    (sp/setval (conj ks sp/BEGINNING) v db)))


(rf/reg-event-db
  :data-into
  (fn [db [_ ks v]]
    (sp/setval (conj ks sp/BEFORE-ELEM) (:data v) db)))


;(rf/reg-event-fx
;  :force-reload!
;  (fn [_ _]
;    (.reload (.-location js/window) true)))


(rf/reg-event-fx
  :log/debug
  (fn [_ [_ msg]]
    (timbre/debug msg)))


(rf/reg-event-fx
  :log/info
  (fn [_ [_ msg]]
    (timbre/info msg)))


(rf/reg-event-fx
  :log/warn
  (fn [_ [_ msg]]
    (timbre/warn msg)))


(rf/reg-event-fx
  :log/error
  (fn [_ [_ msg]]
    (timbre/error msg)))
