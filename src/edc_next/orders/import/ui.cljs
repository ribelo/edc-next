(ns edc-next.orders.import.ui
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(defn mm-list-dialog []
  (let [show? @(rf/subscribe [:orders.import/show-import-dialog?])
        file-list @(rf/subscribe [:orders.import/mm-file-list])]
    [rnp/portal
     [rnp/dialog {:visible    show?
                  :on-dismiss #(rf/dispatch [:orders.import/show-import-dialog false])}
      [rnp/dialog-title "odbierz mm"]
      [rnp/dialog-content
       (do
         (for [file file-list]
           ^{:key file}
           [rnp/list-item {:title    file
                           :on-press (fn []
                                       (rf/dispatch [:do
                                                     [:sync/ftp->collector file]
                                                     [:rn/vibrate 100]
                                                     [:snackbar/show "odebrano mm z ftp" :ok]]))}]))]
      [rnp/dialog-actions
       [rnp/button {:on-press #(rf/dispatch [:orders.import/show-import-dialog false])}
        "anuluj"]]]]))
