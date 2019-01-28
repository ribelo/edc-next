(ns edc-next.ws.subs
  (:require [reagent.core :as r]
            [reagent.ratom :refer [reaction]]
            [com.rpl.specter :as sp]
            [re-frame.core :as rf]))


(rf/reg-sub
  :ws/open?
  (fn [db _]
    (sp/select-one [:_sente/chsk-state :open?] db)))


(rf/reg-sub
  :ws/uid
  (fn [db _]
    (sp/select-one [:_sente/chsk-state :uid] db)))


(rf/reg-sub
  :ws/url
  (fn [db _]
    (sp/select-one [:_sente/chsk :ws-chsk-opts :url] db)))



