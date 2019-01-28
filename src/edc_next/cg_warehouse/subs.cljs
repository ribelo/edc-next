(ns edc-next.cg-warehouse.subs
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(rf/reg-sub
  :cg-warehouse/products.by-ean
  (fn [db _]
    (sp/select-one [:cg-warehouse :_products/by-ean] db)))


(rf/reg-sub
  :cg-warehouse/product
  :<- [:cg-warehouse/products.by-ean]
  (fn [products [_ ean]]
    (sp/select-one [ean] products)))


(rf/reg-sub
  :cg-warehouse/product.price-2
  :<- [:cg-warehouse/products.by-ean]
  (fn [products [_ ean]]
    (sp/select-one [ean :price-2] products)))


(rf/reg-sub
  :cg-warehouse/products.nth
  :<- [:cg-warehouse/products.by-ean]
  (fn [products [_ n]]
    (nth (vals products) n)))


(rf/reg-sub
  :cg-warehouse/products.eans
  :<- [:cg-warehouse/products.by-ean]
  (fn [products _]
    (or (keys products) '())))


(rf/reg-sub
  :cg-warehouse/products.count
  :<- [:cg-warehouse/products.by-ean]
  (fn [products _]
    (count products)))
