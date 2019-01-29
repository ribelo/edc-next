(ns edc-next.orders.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [oops.core :refer [oget]]
            [net.cgrand.xforms :as x]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.db.core :refer [->async-storage]]
            [edc-next.firebase.core :as fb]
            [edc-next.utils.core :as utils]
            [edc-next.orders.creator.events]
            [edc-next.orders.settings.events]
            [edc-next.orders.import.events]))


(rf/reg-event-db
  :orders/show-search-bar
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:orders :_show-search-bar?] val db)
      (sp/transform [:orders :_show-search-bar?] not db))))


(rf/reg-event-fx
  :orders/set-search-value.tmp
  (fn [{db :db} [_ val]]
    {:db       (->> db
                    (sp/setval [:orders :_search-value.tmp] val))
     :dispatch [:orders/set-search-value val]
     }))


(rf/reg-event-db
  :orders/set-search-value
  (fn [db [_ val ms]]
    (when-let [old-timeout (sp/select-one [:orders :_search-timeout] db)]
      (e/tf-cancel! old-timeout))
    (let [timeout (e/after-timeout
                    (or ms 300) (rf/dispatch [:orders/search-product val]))]
      (->> db
           (sp/setval [:orders :_search-value] val)
           (sp/setval [:orders :_search-timeout] timeout)))))


(rf/reg-event-db
  :orders/show-add-document-dialog
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:orders :_show-add-document-dialog?] val db)
      (sp/transform [:orders :_show-add-document-dialog?] not db))))


(rf/reg-event-fx
  :orders/show-only-ordered
  (fn [{db :db} [_ val]]
    (if-not (nil? val)
      (if val
        (let [document-id (sp/select-one [:orders :_document-id] db)
              eans (sp/select [:orders :_documents/by-id document-id
                               :products sp/MAP-VALS :ean] db)]
          {:db (->> db
                    (sp/setval [:orders :_show-only-ordered?] val)
                    (sp/setval [:orders :_view] eans))})
        {:db       (sp/setval [:orders :_show-only-ordered?] val db)
         :dispatch ^:flush-dom [:orders/reset-view]})
      (let [show-only-ordered? (sp/select-one [:orders :_show-only-ordered?] db)]
        {:dispatch [:orders/show-only-ordered (not show-only-ordered?)]}))))


(rf/reg-event-fx
  :orders/search-product
  (fn [{db :db} [_ search-value]]
    (let [warehouse (sp/select-one [:warehouse :_products/by-ean] db)]
      (if (seq search-value)
        (if (js/isNaN search-value)
          (let [eans (into [] (keep (fn [[_ product]]
                                      (when (re-find (js/RegExp. search-value "i") (:name product))
                                        (:ean product)))) warehouse)]
            {:db (->> db
                      (sp/setval [:orders :_view] eans)
                      (sp/setval [:orders :_show-only-ordered?] false))})
          (let [eans (into [] (keep (fn [[ean _]]
                                      (when (re-find (js/RegExp. search-value) ean) ean)))
                           warehouse)]
            {:db (->> db
                      (sp/setval [:orders :_view] eans)
                      (sp/setval [:orders :_show-only-ordered?] false))}))
        {:db       (->> db (sp/setval [:orders :_show-only-ordered?] false))
         :dispatch [:orders/reset-view]}))))


(rf/reg-event-db
  :orders/reset-view
  (fn [db _]
    (let [sorted (->> (sp/select-one [:warehouse :_products/by-ean] db)
                      (x/into []
                              (comp
                                (map second)
                                (x/sort-by (fn [{:keys [optimal stock]}]
                                             (- stock optimal)))
                                (map :ean))))]
      (sp/setval [:orders :_view] sorted db))))


(rf/reg-event-fx
  :orders/init-view
  (fn [_ _]
    {:dispatch [:orders/reset-view]}))


(rf/reg-event-fx
  :orders/new-document
  (fn [{db :db} [_ name]]
    (let [market-id (sp/select-one [:server :_connected-id] db)
          collection (str market-id "-orders")
          id (e/uuid-str)
          doc {:id       id
               :name     (or (not-empty name) "zamówienie ec")
               :time     (js/Date.)
               :products {}}]
      {:firestore/set {:path       [collection id]
                       :doc        doc
                       :on-success [:do
                                    [:orders/select-document doc]
                                    [:orders/show-documents-dialog false]]}})))


(rf/reg-event-fx
  :orders/remove-document
  (fn [{db :db} [_ {:keys [id]}]]
    (let [market-id (sp/select-one [:server :_connected-id] db)
          collection (str market-id "-orders")]
      {:firestore/delete {:path [collection id]}})))


(rf/reg-event-db
  :orders/select-document
  [->async-storage]
  (fn [db [_ {:keys [id]}]]
    (sp/setval [:orders :_document-id] id db)))


(rf/reg-event-fx
  :orders/subscribe-documents
  (fn [{db :db} [_ market-id]]
    (let [collection (str market-id "-orders")]
      {:firestore/subscribe-coll {:path     collection
                                  :dispatch [:orders/subscribe-documents.on-snapshot]}})))


(rf/reg-event-db
  :orders/subscribe-documents.on-snapshot
  (fn [db [_ data]]
    (let [documents (into {} (map (fn [{:keys [id] :as doc}] {id doc})) data)]
      (sp/setval [:orders :_documents/by-id] documents db))))


(rf/reg-event-fx
  :orders/unsubscribe-documents
  (fn [{db :db} [_ market-id]]
    (let [collection (str market-id "-orders")]
      {:firestore/unsubscribe-coll {:path     collection
                                    :dispatch [:clean-up [:orders :_documents/by-id]]}})))


(rf/reg-event-db
  :orders/show-documents-dialog
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:orders :_show-documents-dialog?] val db)
      (sp/transform [:orders :_show-documents-dialog?] not db))))



(rf/reg-event-fx
  :orders/change-document.qty
  (fn [{db :db} [_ doc-id {:keys [name ean]} qty]]
    (let [market-id (sp/select-one [:server :_connected-id] db)
          collection (str market-id "-orders")
          product-qty {:name name
                       :qty  qty
                       :ean  ean}]
      {:firestore/update {:path [collection doc-id]
                          :doc  {(str "products." ean) (if (zero? qty)
                                                         (fb/delete-field)
                                                         product-qty)}}})))


(rf/reg-event-fx
  :orders/document.supply-days.dec
  (fn [{db :db} [_ doc-id {:keys [ean stock pace]} qty]]
    (let [supply-days (/ (+ (max 0 stock) qty) pace)
          qty (e/round0 (* (dec supply-days) pace))]
      {:dispatch [:orders/change-document.qty doc-id {:ean ean} qty]})))


(rf/reg-event-fx
  :orders/document.supply-days.inc
  (fn [{db :db} [_ doc-id {:keys [ean stock pace]} qty]]
    (let [supply-days (/ (+ (max 0 stock) qty) pace)
          qty (e/round0 (* (inc supply-days) pace))]
      {:dispatch [:orders/change-document.qty doc-id {:ean ean} qty]})))


(rf/reg-event-fx
  :orders/barcode-detected
  (fn [{db :db} [_ barcode]]
    {:dispatch-n     [[:camera/show-preview false]
                      [:orders/set-search-value.tmp (oget barcode "data")]
                      [:orders/show-search-bar true]]
     :dispatch-later [{:dispatch [:rn/keyboard-dismiss]
                       :ms       250}]}))
