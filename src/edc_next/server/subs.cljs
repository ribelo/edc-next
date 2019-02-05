(ns edc-next.server.subs
  (:require [re-frame.core :as rf]
            [com.rpl.specter :as sp]))


(rf/reg-sub
  :server/connecting?
  (fn [db _]
    (sp/select-one [:server :_connecting?] db)))


(rf/reg-sub
  :server/servers.by-id
  (fn [db _]
    (sp/select-one [:server :servers/by-id] db)))


(rf/reg-sub
  :server/connected-server.id
  (fn [db _]
    (sp/select-one [:server :_connected-id] db)))


(rf/reg-sub
  :server/connected-server
  :<- [:server/servers.by-id]
  :<- [:server/connected-server.id]
  (fn [[servers id]]
    (sp/select-one id servers)))


(rf/reg-sub
  :server/show-new-server-dialog?
  (fn [db _]
    (sp/select-one [:server :_show-new-server-dialog?] db)))


(rf/reg-sub
  :server/show-disconnect-dialog?
  (fn [db _]
    (sp/select-one [:server :_show-disconnect-dialog?] db)))


(rf/reg-sub
  :server/new-server-name
  (fn [db _]
    (sp/select-one [:server :_new-server-name] db)))


(rf/reg-sub
  :server/new-server-host
  (fn [db _]
    (sp/select-one [:server :_new-server-host] db)))