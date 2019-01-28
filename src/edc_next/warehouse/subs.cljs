(ns edc-next.warehouse.subs
  (:require [reagent.core :as r]
            [reagent.ratom :refer [reaction]]
            [re-frame.core :as rf]
            [com.rpl.specter :as sp]
            [net.cgrand.xforms :as x]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(rf/reg-sub
  :warehouse/products.by-ean
  (fn [db _]
    (sp/select-one [:warehouse :_products/by-ean] db)))


(rf/reg-sub
  :warehouse/product
  :<- [:warehouse/products.by-ean]
  (fn [products [_ ean]]
    (sp/select-one [ean] products)))


(rf/reg-sub
  :warehouse/products.nth
  :<- [:warehouse/products.by-ean]
  (fn [products [_ n]]
    (nth (vals products) n)))


(rf/reg-sub
  :warehouse/products.eans
  :<- [:warehouse/products.by-ean]
  (fn [products _]
    (or (keys products) '())))


(rf/reg-sub
  :warehouse/products.count
  :<- [:warehouse/products.by-ean]
  (fn [products _]
    (count products)))


(rf/reg-sub
  :warehouse/categories
  (fn [db _]
    (sp/select-one [:warehouse :categories] db)))


(rf/reg-sub
  :warehouse/categories.level
  :<- [:warehouse/categories]
  (fn [categories [_ level]]
    (into (sorted-map)
          (comp (filter (fn [[k v]] (= (* 2 (+ level 1)) (count k))))
                (x/sort-by second))
          categories)))


(rf/reg-sub-raw
  :warehouse/categories.parents
  (fn [_]
    (rf/subscribe [:warehouse/categories.level 0])))


(rf/reg-sub
  :warehouse/category.sub-name
  (fn [db [_ category-id]]
    (sp/select-one [:warehouse :categories category-id] db)))


(rf/reg-sub-raw
  :warehouse/category.full-name
  (fn [db [_ category-id sep]]
    (let [categories (reaction (sp/select-one [:warehouse :categories] @db))
          coll (map #(apply str (take % category-id)) (rest (range 0 (+ (count category-id) 2) 2)))]
      (reaction
        (->> coll
             (map #(get @categories %))
             (clojure.string/join (or sep " - ")))))))


(rf/reg-sub
  :warehouse/data-loading?
  (fn [db _]
    (sp/select-one [:warehouse :_data-loading?] db)))


(comment
  (rf/clear-subscription-cache!)
  @(rf/subscribe [:warehouse/categories.parents])
  )