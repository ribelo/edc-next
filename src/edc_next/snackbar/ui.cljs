(ns edc-next.snackbar.ui
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.snackbar.events]
            [edc-next.snackbar.subs]))


(defn snackbar []
  (let [show? @(rf/subscribe [:snackbar/show?])
        text @(rf/subscribe [:snackbar/text])
        action @(rf/subscribe [:snackbar/action])]
    [rnp/portal
     [rnp/snack-bar {:visible    show?
                     :on-dismiss #(rf/dispatch [:snackbar/hide])
                     :action     action
                     :style      {:margin-bottom 64}}
      text]]))
