(ns edc-next.ec-orders.subs
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.ec-orders.creator.subs]
            [edc-next.ec-orders.settings.subs]))


(rf/reg-sub
  :ec-orders/data-loading?
  :<- [:warehouse/data-loading?]
  :<- [:ec-orders/search-timeout.searching?]
  (fn [[warehouse-loading? searching?]]
    (or warehouse-loading? searching?)))


(rf/reg-sub
  :ec-orders/show-search-bar?
  (fn [db _]
    (sp/select-one [:ec-orders :_show-search-bar?] db)))


(rf/reg-sub
  :ec-orders/search-value
  (fn [db _]
    (sp/select-one [:ec-orders :_search-value] db)))


(rf/reg-sub
  :ec-orders/search-value.tmp
  (fn [db _]
    (sp/select-one [:ec-orders :_search-value.tmp] db)))


(rf/reg-sub
  :ec-orders/search-timeout.searching?
  (fn [db _]
    (if-let [timeout (sp/select-one [:ec-orders :_search-timeout] db)]
      (e/tf-pending? timeout)
      false)))


(rf/reg-sub
  :ec-orders/show-only-ordered?
  (fn [db [_]]
    (sp/select-one [:ec-orders :_show-only-ordered?] db)))


(rf/reg-sub
  :ec-orders/show-documents-dialog?
  (fn [db _]
    (sp/select-one [:ec-orders :_show-documents-dialog?] db)))


(rf/reg-sub
  :ec-orders/documents.by-id
  (fn [db _]
    (sp/select-one [:ec-orders :_documents/by-id] db)))


(rf/reg-sub
  :ec-orders/selected-document.id
  (fn [db [_]]
    (sp/select-one [:ec-orders :_document-id] db)))


(rf/reg-sub
  :ec-orders/selected-document
  :<- [:ec-orders/documents.by-id]
  :<- [:ec-orders/selected-document.id]
  (fn [[documents document-id]]
    (sp/select-one document-id documents)))


(rf/reg-sub
  :ec-orders/selected-document.name
  :<- [:ec-orders/documents.by-id]
  :<- [:ec-orders/selected-document.id]
  (fn [[documents id]]
    (sp/select-one [id :name] documents)))


(rf/reg-sub
  :ec-orders/selected-document.time
  :<- [:ec-orders/documents.by-id]
  :<- [:ec-orders/selected-document.id]
  (fn [[documents id]]
    (sp/select-one [id :time] documents)))


(rf/reg-sub
  :ec-orders/selected-document.qty
  :<- [:ec-orders/documents.by-id]
  :<- [:ec-orders/selected-document.id]
  (fn [[documents id] [_ ean]]
    (sp/select-one [id :products (keyword ean) :qty (sp/nil->val 0)] documents)))


(rf/reg-sub
  :ec-orders/view
  (fn [db _]
    (sp/select-one [:ec-orders :_view] db)))

