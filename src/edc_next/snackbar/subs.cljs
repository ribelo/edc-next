(ns edc-next.snackbar.subs
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(rf/reg-sub
  :snackbar/show?
  (fn [db _]
    (sp/select-one [:_snackbar :show?] db)))


(rf/reg-sub
  :snackbar/text
  (fn [db _]
    (or (sp/select-one [:_snackbar :text] db) "")))


(rf/reg-sub
  :snackbar/action
  (fn [db _]
    (sp/select-one [:_snackbar :action] db)))
