(ns edc-next.ws.events
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as rf]
            [taoensso.sente :as sente :refer [cb-success?]]
            [taoensso.timbre :as timbre :refer-macros [tracef debugf infof warnf errorf]]
            [taoensso.encore :as e :refer-macros [have have?]]
            [taoensso.sente :as sente :refer [cb-success?]]
            [taoensso.sente.packers.transit :as sente-transit]
            [cljs.core.async :as async :refer [<! >! put! chan] :refer-macros [go go-loop]]
            [edc-next.transit :as t]
            [edc-next.utils.core :as u]
            [edc-next.ws.fx :refer [event-msg-handler]]))


(rf/reg-event-fx
  :chsk/handshake
  (fn [_ [_ ?data]]
    (timbre/info "Handshake:" ?data)))


(rf/reg-event-fx
  :ws-no-on-success
  (fn [_ [_ event]]
    (timbre/warn :ws-no-on-success event)))


(rf/reg-event-fx
  :ws-no-on-failure
  (fn [_ [_ event]]
    (timbre/warn :ws-no-on-failure event)))


(rf/reg-event-fx
  :sente/make-channel-socket-client!
  (fn [{db :db} [_ url user-id]]
    (u/check-and-throw (s/tuple string? (s/nilable string?)) [url user-id])
    (let [packer (sente-transit/->TransitPacker :json t/writers t/readers)
          url* (e/path url "/chsk")
          params {:type        :auto
                  :packer      packer
                  :client-uuid (or user-id (e/uuid-str))}]
      (let [{:keys [chsk ch-recv send-fn state]} (sente/make-channel-socket-client! url* params)]
        (add-watch state :ws/chsk-state
                   (fn [_ _ old-state new-state]
                     (swap! re-frame.db/app-db
                            assoc :_sente/chsk-state new-state)
                     (when (not= (:last-ws-error new-state) (:last-ws-error old-state))
                       (rf/dispatch [:sente/make-channel-socket-client!.failure]))
                     (when (and (:open? new-state) (not= (:open? new-state) (:open? old-state)))
                       (rf/dispatch [:sente/make-channel-socket-client!.success]))))
        {:db (assoc db
               :_sente/chsk chsk
               :_sente/ch-chsk ch-recv
               :_sente/chsk-send! send-fn
               :_sente/chsk-state @state
               :_sente/router nil)}))))


(rf/reg-event-fx
  :sente/make-channel-socket-client!.success
  (fn [{db :db} [_ {:keys [chsk ch-recv send-fn state]}]]
    (println :sente/make-channel-socket-client!.success)))


(rf/reg-event-fx
  :sente/make-channel-socket-client!.failure
  (fn [{db :db} _]
    (println :sente/make-channel-socket-client!.failure)))  ;TODO


(rf/reg-event-fx
  :sente/stop-router!
  (fn [{{stop-f :_sente/router} :db} _]
    (when stop-f (stop-f))))


(rf/reg-event-fx
  :sente/start-router!
  (fn [{{ch-chsk :_sente/ch-chsk :as db} :db} _]
    (let [router (sente/start-client-chsk-router! ch-chsk event-msg-handler)]
      {:db (assoc db :_sente/router router)})))


(rf/reg-event-fx
  :sente/disconnect!
  (fn [{{chsk :_sente/chsk :as db} :db} _]
    (when chsk
      (sente/chsk-disconnect! chsk)
      {:db (assoc db
             :_sente/chsk nil
             :_sente/ch-chsk nil
             :_sente/chsk-send! nil
             :_sente/chsk-state nil
             :_sente/router nil)})))


(rf/reg-event-fx
  :sente/reconnect!
  (fn [{{chsk :_sente/chsk} :db} _]
    (when chsk
      {:sente/reconnect! chsk})))


(rf/reg-event-fx
  :ws/start
  (fn [_ [_ url user-id]]
    {:async-flow {:first-dispatch [:sente/make-channel-socket-client! url user-id]
                  :rules          [{:when     :seen? :events :sente/make-channel-socket-client!
                                    :dispatch [:sente/stop-router!]}
                                   {:when     :seen? :events :sente/stop-router!
                                    :dispatch [:sente/start-router!]
                                    :halt?    true}]}}))


(rf/reg-event-fx
  :ws/reconnect
  (fn [_ [_ url user-id]]
    {:async-flow {:first-dispatch [:sente/stop-router!]
                  :rules          [{:when     :seen? :events :sente/stop-router!
                                    :dispatch [:sente/disconnect!]}
                                   {:when     :seen? :events :sente/disconnect!
                                    :dispatch [:sente/make-channel-socket-client! url user-id]}
                                   {:when     :seen? :events :sente/make-channel-socket-client!
                                    :dispatch [:sente/start-router!]
                                    :halt?    true}]}}))

(rf/reg-event-fx
  :ws/send
  (fn [_ [_ {:keys [event on-success on-failure timeout params]
             :or   {params {} timeout 10000} :as send}]]
    {:ws/send send}))

