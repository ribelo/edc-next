(ns edc-next.ec-orders.db
  (:require [cljs.spec.alpha :as s]
            [edc-next.ec-orders.creator.db :as creator.db]
            [edc-next.ec-orders.settings.db :as settings.db]))


(def state (merge-with merge {:ec-orders {:_show-search-bar?       false
                                          :_search-value           nil
                                          :_search-value.tmp       nil
                                          :_search-timeout         nil
                                          :_show-only-ordered?     false
                                          :_show-documents-dialog? false
                                          :_documents/by-id        {}
                                          :_document-id            nil
                                          :_view                   []}}
                       creator.db/state
                       settings.db/state))

;(s/def ::min-pace (s/and number? pos?))
;(s/def ::min-margin number?)
;(s/def ::only-below-minimum? boolean?)
;(s/def ::_show-only-ordered? boolean?)
;(s/def ::_show-dialog? boolean?)
;(s/def ::_document-id string?)
;;(s/def ::_view coll?)
;(s/def ::selected-categories (s/map-of string? boolean?))
;(s/def ::card-columns int?)
;(s/def ::product-card (s/map-of keyword? boolean?))
;(s/def ::ec-orders (s/keys :req [::_document-id]
;                           :req-un [::min-pace
;                                    ::min-margin
;                                    ::only-below-minimum?
;                                    ::_show-dialog?
;                                    ::selected-categories
;                                    ::card-columns
;                                    ::product-card]))
;
;(s/def :db.spec/orders (s/keys :req-un [::ec-orders]))