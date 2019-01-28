(ns edc-next.ws.fx
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as rf]
            [taoensso.timbre :as timbre]
            [taoensso.sente :as sente :refer [cb-success?]]
            [taoensso.encore :as e :refer-macros [have have?]]))


(s/def ::event (s/and vector? #(keyword? (first %))))
(s/def ::fn #(= js/Function (type %)))

(s/def ::send-dispatch (s/or :events (s/+ ::event)
                             :fn ::fn
                             :fns (s/+ ::fn)
                             :event ::event))


(defmulti -change-state-handler (fn [{:keys [first-open? open?]}]
                                  [first-open? open?]))


(defmethod -change-state-handler :default
  [state]
  (timbre/warn "Unhandled state change: %s" state))


(defmethod -change-state-handler [false true]
  [state]
  (rf/dispatch [:server/reconnect]))


(defmulti -event-msg-handler :id)


(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (timbre/debug "Try to handle event:" id ?data)
  (-event-msg-handler ev-msg))


(defmethod -event-msg-handler :default                      ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  (timbre/warn "Unhandled event: %s" event))


(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (if (:first-open? new-state-map)
      (timbre/info "Channel socket successfully established!: %s" new-state-map)
      (-change-state-handler new-state-map))))


(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg [evt dispatch] :?data}]
  (timbre/debug "Push event from server:" evt dispatch)
  (cond
    (= :chsk/ws-ping evt) (timbre/info "Ping:" (js/Date.))
    (= :rf/dispatch evt) (rf/dispatch dispatch)
    :else (timbre/warn "Unhandled event: %s" evt)))


(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (rf/dispatch [:chsk/handshake ?data])))



(defn send! [{:keys [event params on-success on-failure timeout]
              :or   {params  {}
                     timeout 60000}}]
  (let [chsk-send! (:_sente/chsk-send! @re-frame.db/app-db)
        on-success (or on-success [:ws-no-on-success event])
        on-failure (or on-failure [:ws-no-on-failure event])
        [success-type success-fn] (s/conform ::send-dispatch on-success)
        [failure-type failure-fn] (s/conform ::send-dispatch on-failure)]
    (chsk-send! [event params] timeout
                (fn [cb-reply]
                  (if (cb-success? cb-reply)
                    (case success-type
                      :events (doseq [ev success-fn]
                                (rf/dispatch (conj ev cb-reply)))
                      :fn (on-success cb-reply)
                      :fns (doseq [f success-fn]
                             (f cb-reply))
                      :event (rf/dispatch (conj success-fn cb-reply)))
                    (case failure-type
                      :events (doseq [ev failure-fn]
                                (rf/dispatch (conj ev cb-reply)))
                      :fn (failure-fn cb-reply)
                      :fns (doseq [f failure-fn]
                             (f cb-reply))
                      :event (rf/dispatch (conj failure-fn cb-reply))))))))


(rf/reg-fx :ws/send send!)
