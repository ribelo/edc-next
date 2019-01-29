(ns edc-next.orders.import.subs
  (:require [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]))


(rf/reg-sub
  :orders.import/show-import-dialog?
  (fn [db _]
    (sp/select-one [:orders :_show-import-dialog?] db)))


(rf/reg-sub
  :orders.import/mm-file-list
  (fn [db _]
    (sp/select-one [:orders :_mm-file-list] db)))
