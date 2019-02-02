(ns edc-next.orders.creator.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.db.core :refer [->async-storage]]))


(rf/reg-event-db
  :orders.creator/set-supplier
  (fn [db [_ supplier]]
    (case supplier
      "ec" (-> db
               (assoc-in [:orders :creator :_supplier] supplier)
               (assoc-in [:orders :creator :_only-cheaper-than-ec?] false)
               (assoc-in [:orders :creator :_only-cheaper-than-cg?] true)
               (assoc-in [:orders :creator :_only-in-cg-stock?] false))
      "cg" (-> db
               (assoc-in [:orders :creator :_supplier] supplier)
               (assoc-in [:orders :creator :_only-cheaper-than-ec?] true)
               (assoc-in [:orders :creator :_only-cheaper-than-cg?] false)
               (assoc-in [:orders :creator :_only-in-cg-stock?] true)))))


(rf/reg-event-db
  :orders.creator/show-make-order-dialog
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:orders :creator :_show-make-order-dialog?] val db)
      (sp/transform [:orders :creator :_show-make-order-dialog?] not db))))


(rf/reg-event-db
  :orders.creator/set-only-below-minimum
  [->async-storage]
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:orders :creator :only-below-minimum?] val db)
      (sp/transform [:orders :creator :only-below-minimum?] not db))))


(rf/reg-event-db
  :orders.creator/toggle-category
  [->async-storage]
  (fn [db [_ id]]
    (sp/transform [:orders :creator :selected-categories id] not db)))


(rf/reg-event-db
  :orders.creator/set-min-pace
  [->async-storage]
  (fn [db [_ pace]]
    (sp/setval [:orders :creator :min-pace] (js/parseFloat pace) db)))


(rf/reg-event-db
  :orders.creator/inc-min-pace
  [->async-storage]
  (fn [db _]
    (sp/transform [:orders :creator :min-pace] #(e/round2 (+ % 0.1)) db)))


(rf/reg-event-db
  :orders.creator/dec-min-pace
  [->async-storage]
  (fn [db _]
    (sp/transform [:orders :creator :min-pace] #(e/round2 (- % 0.1)) db)))


(rf/reg-event-db
  :orders.creator/inc-min-margin
  [->async-storage]
  (fn [db _]
    (sp/transform [:orders :creator :min-margin] #(e/round2 (+ % 0.05)) db)))


(rf/reg-event-db
  :orders.creator/dec-min-margin
  [->async-storage]
  (fn [db _]
    (sp/transform [:orders :creator :min-margin] #(e/round2 (- % 0.05)) db)))


(rf/reg-event-db
  :orders.creator/set-only-cheaper-than-cg
  [->async-storage]
  (fn [db [_ val]]
    (let [cg-state (sp/select-one [:orders :creator :_only-cheaper-than-cg?] db)
          ec-state (sp/select-one [:orders :creator :_only-cheaper-than-ec?] db)]
      (if-not (nil? val)
        (cond->> db
                 true
                 (sp/setval [:orders :creator :_only-cheaper-than-cg?] val)
                 (= val ec-state)
                 (sp/setval [:orders :creator :_only-cheaper-than-ec?] (not val)))
        (cond->> db
                 true
                 (sp/transform [:orders :creator :_only-cheaper-than-cg?] not)
                 (and (not cg-state) (not= cg-state ec-state))
                 (sp/transform [:orders :creator :_only-cheaper-than-ec?] not))))))


(rf/reg-event-db
  :orders.creator/set-only-cheaper-than-ec
  [->async-storage]
  (fn [db [_ val]]
    (let [cg-state (sp/select-one [:orders :creator :_only-cheaper-than-cg?] db)
          ec-state (sp/select-one [:orders :creator :_only-cheaper-than-ec?] db)]
      (if-not (nil? val)
        (cond->> db
                 true
                 (sp/setval [:orders :creator :_only-cheaper-than-ec?] val)
                 (= val ec-state)
                 (sp/setval [:orders :creator :_only-cheaper-than-cg?] (not val)))
        (cond->> db
                 true
                 (sp/transform [:orders :creator :_only-cheaper-than-ec?] not)
                 (and (not ec-state) (not= cg-state ec-state))
                 (sp/transform [:orders :creator :_only-cheaper-than-cg?] not))))))


(rf/reg-event-db
  :orders.creator/set-only-in-cg-stock
  [->async-storage]
  (fn [db [_ val]]
    (println :orders.creator/set-only-in-cg-stock val)
    (if-not (nil? val)
      (sp/setval [:orders :creator :_only-in-cg-stock?] val db)
      (sp/transform [:orders :creator :_only-in-cg-stock?] not db))))

(comment
  (let [db @re-frame.db/app-db]
    ;(first (sp/select-one [:cg-warehouse :_products/by-ean] db))
    (rand-nth (vec (sp/select-one [:warehouse :_products/by-ean] db)))
    ;(e/qb 200000 (re-find #"^F" "F01450_123113"))
    ;(.startsWith "F01450_123113" "F")
    ))


(rf/reg-event-fx
  :orders.creator/make-market-order
  (fn [{db :db} _]
    (let [market-id (sp/select-one [:server :_connected-id] db)
          collection (str market-id "-orders")
          warehouse (sp/select-one [:warehouse :_products/by-ean] db)
          cg-warehouse (sp/select-one [:cg-warehouse :_products/by-ean] db)
          supplier (sp/select-one [:orders :creator :_supplier] db)
          min-margin (sp/select-one [:orders :creator :min-margin] db)
          min-pace (sp/select-one [:orders :creator :min-pace] db)
          only-below-min (sp/select-one [:orders :creator :only-below-minimum?] db)
          selected-categories (sp/select-one [:orders :creator :selected-categories] db)
          only-cheaper-than-ec? (sp/select-one [:orders :creator :_only-cheaper-than-ec?] db)
          only-cheaper-than-cg? (sp/select-one [:orders :creator :_only-cheaper-than-cg?] db)
          only-in-cg-stock? (sp/select-one [:orders :creator :_only-in-cg-stock?] db)
          doc-id (sp/select-one [:orders :_document-id] db)
          products (into {}
                         (comp
                           (filter (if (= "ec" supplier)
                                     (fn [[_ {:keys [id]}]]
                                       (not (.startsWith "F" id)))
                                     identity))
                           (filter (if only-below-min
                                     (fn [[_ {:keys [stock min-supply pace]}]]
                                       (< stock (* min-supply pace)))
                                     identity))
                           (filter (if only-cheaper-than-cg?
                                     (fn [[ean {:keys [buy-price]}]]
                                       (let [cg-price (sp/select-one [ean :buy-price] cg-warehouse)]
                                         (or (nil? cg-price) (< ^number buy-price ^number cg-price))))
                                     identity))
                           (filter (if only-cheaper-than-ec?
                                     (fn [[ean {:keys [buy-price]}]]
                                       (let [cg-price (sp/select-one [ean :buy-price] cg-warehouse)]
                                         (and cg-price (>= ^number buy-price ^number cg-price))))
                                     identity))
                           (filter (if only-in-cg-stock?
                                     (fn [[ean _]]
                                       (let [stock (sp/select-one [ean :stock] cg-warehouse)]
                                         (and stock (pos? ^number stock))))
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
      (println (count products))
      {:db               (->> db
                              (sp/setval [:orders :creator :_calculating?] true)
                              (sp/setval [:orders :creator :_show-make-order-dialog?] false))
       :firestore/update {:path       [collection doc-id]
                          :doc        {:products products}
                          :on-success [:orders.creator/make-market-order.success]}})))


(rf/reg-event-fx
  :orders.creator/make-market-order.success
  (fn [{db :db} _]
    {:db       (sp/setval [:orders :creator :_calculating?] false db)
     :dispatch [:orders/show-only-ordered true]}))
