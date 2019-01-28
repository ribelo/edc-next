(ns edc-next.ec-orders.creator.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.db.core :refer [->async-storage]]))


(rf/reg-event-db
  :ec-orders.creator/show-make-order-dialog
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:ec-orders :creator :_show-make-order-dialog?] val db)
      (sp/transform [:ec-orders :creator :_show-make-order-dialog?] not db))))


(rf/reg-event-db
  :ec-orders.creator/set-only-below-minimum
  [->async-storage]
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:ec-orders :creator :only-below-minimum?] val db)
      (sp/transform [:ec-orders :creator :only-below-minimum?] not db))))


(rf/reg-event-db
  :ec-orders.creator/toggle-category
  [->async-storage]
  (fn [db [_ id]]
    (sp/transform [:ec-orders :creator :selected-categories id] not db)))


(rf/reg-event-db
  :ec-orders.creator/set-min-pace
  [->async-storage]
  (fn [db [_ pace]]
    (sp/setval [:ec-orders :creator :min-pace] (js/parseFloat pace) db)))


(rf/reg-event-db
  :ec-orders.creator/inc-min-pace
  [->async-storage]
  (fn [db _]
    (sp/transform [:ec-orders :creator :min-pace] #(e/round2 (+ % 0.1)) db)))


(rf/reg-event-db
  :ec-orders.creator/dec-min-pace
  [->async-storage]
  (fn [db _]
    (sp/transform [:ec-orders :creator :min-pace] #(e/round2 (- % 0.1)) db)))


(rf/reg-event-db
  :ec-orders.creator/inc-min-margin
  [->async-storage]
  (fn [db _]
    (sp/transform [:ec-orders :creator :min-margin] #(e/round2 (+ % 0.05)) db)))


(rf/reg-event-db
  :ec-orders.creator/dec-min-margin
  [->async-storage]
  (fn [db _]
    (sp/transform [:ec-orders :creator :min-margin] #(e/round2 (- % 0.05)) db)))


(rf/reg-event-db
  :ec-orders.creator/set-only-cheaper-than-cg
  [->async-storage]
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:ec-orders :creator :only-cheaper-than-cg?] val db)
      (sp/transform [:ec-orders :creator :only-cheaper-than-cg?] not db))))


(rf/reg-event-fx
  :ec-orders.creator/make-market-order
  (fn [{db :db} _]
    (let [market-id (sp/select-one [:server :_connected-id] db)
          collection (str market-id "-orders")
          warehouse (sp/select-one [:warehouse :_products/by-ean] db)
          min-margin (sp/select-one [:ec-orders :creator :min-margin] db)
          min-pace (sp/select-one [:ec-orders :creator :min-pace] db)
          only-below-min (sp/select-one [:ec-orders :creator :only-below-minimum?] db)
          selected-categories (sp/select-one [:ec-orders :creator :selected-categories] db)
          doc-id (sp/select-one [:ec-orders :_document-id] db)
          products (into {}
                         (comp
                           (filter (if only-below-min
                                     (fn [[_ {:keys [stock min-supply pace]}]]
                                       (< stock (* min-supply pace)))
                                     identity))
                           (filter (fn [[_ {:keys [stock optimal]}]]
                                     (> (e/round0 (- optimal (max 0 stock))) 0)))
                           (filter (fn [[_ {:keys [margin]}]] (>= margin min-margin)))
                           (filter (fn [[_ {:keys [pace]}]] (>= pace min-pace)))
                           (filter (fn [[_ {:keys [category-id]}]]
                                     (get selected-categories (e/get-substr category-id 0 2))))
                           (map (fn [[ean {:keys [name id qty optimal]}]]
                                  {ean {:name name
                                        :id   id
                                        :ean  ean
                                        :qty  (e/round0 (- optimal (max 0 qty)))}}))
                           )
                         warehouse)]
      {:firestore/update {:path       [collection doc-id]
                          :doc        {:products products}
                          :on-success [:ec-orders.creator/make-market-order.success]}})))


(rf/reg-event-fx
  :ec-orders.creator/make-market-order.success
  (fn [{db :db} _]
    {:db (sp/setval [:ec-orders :creator :_show-dialog?] false db)}))
