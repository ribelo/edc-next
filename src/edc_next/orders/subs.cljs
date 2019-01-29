(ns edc-next.orders.subs
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.orders.creator.subs]
            [edc-next.orders.settings.subs]
            [edc-next.orders.import.subs]))


(rf/reg-sub
  :orders/data-loading?
  :<- [:warehouse/data-loading?]
  :<- [:orders/search-timeout.searching?]
  (fn [[warehouse-loading? searching?]]
    (or warehouse-loading? searching?)))


(rf/reg-sub
  :orders/show-search-bar?
  (fn [db _]
    (sp/select-one [:orders :_show-search-bar?] db)))


(rf/reg-sub
  :orders/search-value
  (fn [db _]
    (sp/select-one [:orders :_search-value] db)))


(rf/reg-sub
  :orders/search-value.tmp
  (fn [db _]
    (sp/select-one [:orders :_search-value.tmp] db)))


(rf/reg-sub
  :orders/search-timeout.searching?
  (fn [db _]
    (if-let [timeout (sp/select-one [:orders :_search-timeout] db)]
      (e/tf-pending? timeout)
      false)))


(rf/reg-sub
  :orders/show-only-ordered?
  (fn [db [_]]
    (sp/select-one [:orders :_show-only-ordered?] db)))


(rf/reg-sub
  :orders/show-documents-dialog?
  (fn [db _]
    (sp/select-one [:orders :_show-documents-dialog?] db)))


(rf/reg-sub
  :orders/documents.by-id
  (fn [db _]
    (sp/select-one [:orders :_documents/by-id] db)))


(rf/reg-sub
  :orders/selected-document.id
  (fn [db [_]]
    (sp/select-one [:orders :_document-id] db)))


(rf/reg-sub
  :orders/selected-document
  :<- [:orders/documents.by-id]
  :<- [:orders/selected-document.id]
  (fn [[documents document-id]]
    (sp/select-one document-id documents)))


(rf/reg-sub
  :orders/selected-document.name
  :<- [:orders/documents.by-id]
  :<- [:orders/selected-document.id]
  (fn [[documents id]]
    (sp/select-one [id :name] documents)))


(rf/reg-sub
  :orders/selected-document.time
  :<- [:orders/documents.by-id]
  :<- [:orders/selected-document.id]
  (fn [[documents id]]
    (sp/select-one [id :time] documents)))


(rf/reg-sub
  :orders/selected-document.qty
  :<- [:orders/documents.by-id]
  :<- [:orders/selected-document.id]
  (fn [[documents id] [_ ean]]
    (sp/select-one [id :products (keyword ean) :qty (sp/nil->val 0)] documents)))


(rf/reg-sub
  :orders/view
  (fn [db _]
    (sp/select-one [:orders :_view] db)))

