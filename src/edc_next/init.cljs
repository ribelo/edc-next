(ns edc-next.init
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as rf]
            [day8.re-frame.async-flow-fx]
            [taoensso.timbre :as timbre]

            [edc-next.firebase.db]
            [edc-next.firebase.fx]
            [edc-next.firebase.events]

            [edc-next.utils.fx]
            [edc-next.utils.events]

            [edc-next.rn.events]

            [edc-next.rnrf.events]
            [edc-next.rnrf.subs]

            [edc-next.db.core :as db]
            [edc-next.db.events]

            [edc-next.theme.db :as themes.db]
            [edc-next.theme.events]
            [edc-next.theme.subs]

            [edc-next.ws.fx]
            [edc-next.ws.events]
            [edc-next.ws.subs]

            [edc-next.sync.events]

            [edc-next.camera.db :as camera.db]
            [edc-next.camera.events]
            [edc-next.camera.subs]

            [edc-next.server.db :as server.db]
            [edc-next.server.events]
            [edc-next.server.subs]

            [edc-next.snackbar.events]
            [edc-next.snackbar.subs]

            [edc-next.warehouse.db :as warehouse.db]
            [edc-next.warehouse.events]
            [edc-next.warehouse.subs]

            [edc-next.cg-warehouse.db :as cg-warehouse.db]
            [edc-next.cg-warehouse.events]
            [edc-next.cg-warehouse.subs]

            [edc-next.orders.db :as orders.db]
            [edc-next.orders.events]
            [edc-next.orders.subs]
            [goog]

            ))


(when goog.DEBUG
  (s/check-asserts true))


(rf/reg-event-fx
  :edc-next/boot
  (fn [_ _]
    {:db         (merge db/default-db
                        themes.db/state
                        camera.db/state
                        server.db/state
                        warehouse.db/state
                        cg-warehouse.db/state
                        orders.db/state)
     :dispatch-n [[:db/load-async-storage!]
                  [:firebase/init]]}))
