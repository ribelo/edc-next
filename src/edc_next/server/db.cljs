(ns edc-next.server.db
  (:require [cljs.spec.alpha :as s]))


(def state
  {:server {:servers/by-id               {"f01451" {:id   1
                                                    :name "wilków-osiedle"
                                                    :host "83.16.153.125:3000"}
                                          "f01450" {:id   "f01450"
                                                    :name "proboszczów"
                                                    :host "79.187.160.57:3000"}
                                          "f01752" {:id   "f01752"
                                                    :name "krotoszyce"
                                                    :host "192.168.1.2:3000"}}
            :_show-new-server-dialog?    false
            :_show-delete-server-dialog? false
            :_connected-id               nil
            :_connecting?                false}})


(s/def :server/id string?)
(s/def :server/name string?)
(s/def :server/host string?)
(s/def ::server (s/keys :req-un [:server/id :server/name :server/host]))
(s/def :servers/by-id (s/map-of string? ::server))
(s/def ::_show-settings-dialog? (s/nilable boolean?))
(s/def ::_show-new-server-dialog? (s/nilable boolean?))
(s/def ::_show-delete-server-dialog? (s/nilable boolean?))
(s/def ::_connected-id (s/nilable :server/id))
(s/def ::_connecting? (s/nilable boolean?))

(s/def :state/server
  (s/keys :req [:servers/by-id]
          :req-un [::_show-settings-dialog?
                   ::_show-new-server-dialog?
                   ::_show-delete-server-dialog?
                   ::_connected-id
                   ::_connecting?]))

(s/def :db/server (s/keys :req-un [:state/server]))
