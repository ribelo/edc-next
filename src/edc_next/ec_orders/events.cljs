(ns edc-next.ec-orders.events
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
            [edc-next.ec-orders.creator.events]
            [edc-next.ec-orders.settings.events]))


(rf/reg-event-db
  :ec-orders/show-search-bar
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:ec-orders :_show-search-bar?] val db)
      (sp/transform [:ec-orders :_show-search-bar?] not db))))


(rf/reg-event-fx
  :ec-orders/set-search-value.tmp
  (fn [{db :db} [_ val]]
    {:db       (->> db
                    (sp/setval [:ec-orders :_search-value.tmp] val))
     :dispatch [:ec-orders/set-search-value val]
     }))


(rf/reg-event-db
  :ec-orders/set-search-value
  (fn [db [_ val ms]]
    (when-let [old-timeout (sp/select-one [:ec-orders :_search-timeout] db)]
      (e/tf-cancel! old-timeout))
    (let [timeout (e/after-timeout
                    (or ms 300) (rf/dispatch [:ec-orders/search-product val]))]
      (->> db
           (sp/setval [:ec-orders :_search-value] val)
           (sp/setval [:ec-orders :_search-timeout] timeout)))))


(rf/reg-event-db
  :ec-orders/show-add-document-dialog
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:ec-orders :_show-add-document-dialog?] val db)
      (sp/transform [:ec-orders :_show-add-document-dialog?] not db))))


(rf/reg-event-fx
  :ec-orders/show-only-ordered
  (fn [{db :db} [_ val]]
    (if-not (nil? val)
      (if val
        (let [document-id (sp/select-one [:ec-orders :_document-id] db)
              eans (sp/select [:ec-orders :_documents/by-id document-id
                               :products sp/MAP-VALS :ean] db)]
          {:db (->> db
                    (sp/setval [:ec-orders :_show-only-ordered?] val)
                    (sp/setval [:ec-orders :_view] eans))})
        {:db       (sp/setval [:ec-orders :_show-only-ordered?] val db)
         :dispatch ^:flush-dom [:ec-orders/reset-view]})
      (let [show-only-ordered? (sp/select-one [:ec-orders :_show-only-ordered?] db)]
        {:dispatch [:ec-orders/show-only-ordered (not show-only-ordered?)]}))))


(rf/reg-event-fx
  :ec-orders/search-product
  (fn [{db :db} [_ search-value]]
    (let [warehouse (sp/select-one [:warehouse :_products/by-ean] db)]
      (if (seq search-value)
        (if (js/isNaN search-value)
          (let [eans (into [] (keep (fn [[_ product]]
                                      (when (re-find (js/RegExp. search-value "i") (:name product))
                                        (:ean product)))) warehouse)]
            {:db (->> db
                      (sp/setval [:ec-orders :_view] eans)
                      (sp/setval [:ec-orders :_show-only-ordered?] false))})
          (let [eans (into [] (keep (fn [[ean _]]
                                      (when (re-find (js/RegExp. search-value) ean) ean)))
                           warehouse)]
            {:db (->> db
                      (sp/setval [:ec-orders :_view] eans)
                      (sp/setval [:ec-orders :_show-only-ordered?] false))}))
        {:db       (->> db (sp/setval [:ec-orders :_show-only-ordered?] false))
         :dispatch [:ec-orders/reset-view]}))))


(rf/reg-event-db
  :ec-orders/reset-view
  (fn [db _]
    (let [sorted (->> (sp/select-one [:warehouse :_products/by-ean] db)
                      (x/into []
                              (comp
                                (map second)
                                (x/sort-by (fn [{:keys [optimal stock]}]
                                             (- stock optimal)))
                                (map :ean))))]
      (sp/setval [:ec-orders :_view] sorted db))))


(rf/reg-event-fx
  :ec-orders/init-view
  (fn [_ _]
    {:dispatch [:ec-orders/reset-view]}))


(rf/reg-event-fx
  :ec-orders/new-document
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
                                    [:ec-orders/select-document doc]
                                    [:ec-orders/show-documents-dialog false]]}})))


(rf/reg-event-fx
  :ec-orders/remove-document
  (fn [{db :db} [_ {:keys [id]}]]
    (let [market-id (sp/select-one [:server :_connected-id] db)
          collection (str market-id "-orders")]
      {:firestore/delete {:path [collection id]}})))


(rf/reg-event-db
  :ec-orders/select-document
  [->async-storage]
  (fn [db [_ {:keys [id]}]]
    (sp/setval [:ec-orders :_document-id] id db)))


(rf/reg-event-fx
  :ec-orders/subscribe-documents
  (fn [{db :db} [_ market-id]]
    (let [collection (str market-id "-orders")]
      {:firestore/subscribe-coll {:path     collection
                                  :dispatch [:ec-orders/subscribe-documents.on-snapshot]}})))


(rf/reg-event-db
  :ec-orders/subscribe-documents.on-snapshot
  (fn [db [_ data]]
    (let [documents (into {} (map (fn [{:keys [id] :as doc}] {id doc})) data)]
      (sp/setval [:ec-orders :_documents/by-id] documents db))))


(rf/reg-event-fx
  :ec-orders/unsubscribe-documents
  (fn [{db :db} [_ market-id]]
    (let [collection (str market-id "-orders")]
      {:firestore/unsubscribe-coll {:path     collection
                                    :dispatch [:clean-up [:ec-orders :_documents/by-id]]}})))


(rf/reg-event-db
  :ec-orders/show-documents-dialog
  (fn [db [_ val]]
    (if-not (nil? val)
      (sp/setval [:ec-orders :_show-documents-dialog?] val db)
      (sp/transform [:ec-orders :_show-documents-dialog?] not db))))



(rf/reg-event-fx
  :ec-orders/change-document.qty
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
  :ec-orders/document.supply-days.dec
  (fn [{db :db} [_ doc-id {:keys [ean stock pace]} qty]]
    (let [supply-days (/ (+ (max 0 stock) qty) pace)
          qty (Math/floor (- (* pace (max 0 (dec (Math/floor supply-days)))) stock))]
      {:dispatch [:ec-orders/change-document.qty doc-id {:ean ean} qty]})))


(rf/reg-event-fx
  :ec-orders/document.supply-days.inc
  (fn [{db :db} [_ doc-id {:keys [ean stock pace]} qty]]
    (let [supply-days (/ (+ (max 0 stock) qty) pace)
          qty (Math/ceil (- (* pace (inc (Math/ceil supply-days))) stock))]
      {:dispatch [:ec-orders/change-document.qty doc-id {:ean ean} qty]})))


(rf/reg-event-fx
  :ec-orders/barcode-detected
  (fn [{db :db} [_ barcode]]
    {:dispatch-n     [[:camera/show-preview false]
                      [:ec-orders/set-search-value.tmp (oget barcode "data")]
                      [:ec-orders/show-search-bar true]]
     :dispatch-later [{:dispatch [:rn/keyboard-dismiss]
                       :ms       250}]}))
