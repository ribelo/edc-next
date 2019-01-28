(ns edc-next.snackbar.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(rf/reg-event-fx
  :snackbar/show
  (fn [{db :db} [_ text action]]
    (if text
      (let [action* (if (= :ok action)
                      {:label   "ok"
                       :onPress #(rf/dispatch [:snackbar/hide])}
                      action)]
        {:db (->> db
                  (sp/setval [:_snackbar :show?] true)
                  (sp/setval [:_snackbar :text] text)
                  (sp/setval [:_snackbar :action] action*))})
      {:dispatch [:snackbar/hide]})))


(rf/reg-event-db
  :snackbar/hide
  (fn [db _]
    (->> db
         (sp/setval [:_snackbar :show?] false)
         (sp/setval [:_snackbar :text] nil)
         (sp/setval [:_snackbar :action] nil))))