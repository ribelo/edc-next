(ns edc-next.server.events
  (:require [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.db.core :refer [->async-storage]]
            [edc-next.server.db :refer [state]]))


(rf/reg-event-db
  :server/init-state
  (fn [db _]
    (merge db state)))


(rf/reg-event-db
  :server/set-server
  (fn [db [_ server]]
    (sp/setval [:server :_connected-id] server db)))


(rf/reg-event-db
  :server/set-conecting
  (fn [db [_ val]]
    (if val
      (sp/setval [:server :_connecting?] val db)
      (sp/transform [:server :_connecting?] not db))))

(comment
  @(rf/subscribe [:warehouse/products.count]))

(rf/reg-event-fx
  :server/connect
  (fn [{db :db} [_ id]]
    (let [host (sp/select-one [:server :servers/by-id id :host] db)]
      {:db         (sp/setval [:server :_connecting?] true db)
       :async-flow {:first-dispatch [:ws/reconnect host]
                    :rules          [{:when       :seen? :events :sente/make-channel-socket-client!.success
                                      :dispatch-n [[:server/set-server id]
                                                   [:rnrf/navigate! :app]]}
                                     {:when       :seen? :events :chsk/handshake
                                      :dispatch-n [[:warehouse/get-products]
                                                   [:cg-warehouse/get-products]
                                                   [:server/set-conecting false]
                                                   [:orders/reset-view]
                                                   [:orders/subscribe-documents id]
                                                   ]
                                      :halt?      true}
                                     ;{:when       :seen? :events :sente/make-channel-socket-client!.failure
                                     ; :dispatch-n [[:server/set-server nil]
                                     ;              [:server/set-conecting false]
                                     ;              [:ui/show-snackbar "brak połączenia z serwerem" :ok]]
                                     ; :halt?      true}
                                     ]}})))


(rf/reg-event-fx
  :server/reconnect
  (fn [{db :db} _]
    (let [market-id (sp/select-one [:server :_connected-id] db)]
      {:dispatch [:server/connect market-id]})))


(rf/reg-event-fx
  :server/disconnect
  [->async-storage]
  (fn [{db :db} _]
    (let [market-id (sp/select-one [:server :_connected-id] db)]
      {:db         (->> db
                        (sp/setval [:warehouse :_products/by-ean] [])
                        (sp/setval [:server :_connected-id] nil))
       :dispatch-n [[:sente/disconnect!]
                    [:rnrf/navigate! :start-screen]
                    [:orders/unsubscribe-documents market-id]
                    [:server/show-disconnect-dialog false]]})))


;(rf/reg-event-fx
;  :server/version
;  (fn [{db :db} _]
;    {:ws/send {:event :server/version}}))


(rf/reg-event-fx
  :server/create-new-server
  [->async-storage]
  (fn [{db :db} [_ market-id name host]]
    (let [server {:id market-id :name name :host host}]
      {:db       (sp/setval [:server :servers/by-id market-id] server db)
       :dispatch [:server/show-new-server-dialog false]})))


(rf/reg-event-fx
  :server/delete-server
  [->async-storage]
  (fn [{db :db} [_ id]]
    {:db       (sp/setval [:server :servers/by-id id] sp/NONE db)
     :dispatch [:server/show-new-server-dialog false]}))


(rf/reg-event-db
  :server/show-disconnect-dialog
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:server :_show-disconnect-dialog?] val db)
      (sp/transform [:server :_show-disconnect-dialog?] not db))))


(rf/reg-event-db
  :server/show-new-server-dialog
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:server :_show-new-server-dialog?] val db)
      (sp/transform [:server :_show-new-server-dialog?] not db))))
