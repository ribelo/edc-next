(ns edc-next.drawer.ui
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(defn drawer []
  (fn []
    [rn/view {:style {:flex 1}}
     [rnp/drawer-section {:title "wybierz scenariusz"}
      [rnp/drawer-item {:label    "zamówienie"
                        :on-press #(rf/dispatch [:rnrf/navigate! :home])}]
      [rnp/drawer-item {:label    "inwentaryzator"
                        :on-press #(rf/dispatch [:rnrf/navigate! :home])}]
      [rnp/drawer-item {:label    "przyjęcie towaru"
                        :on-press #(rf/dispatch [:rnrf/navigate! :home])}]
      [rnp/drawer-item {:label    "warzywa - owoce"
                        :on-press #(rf/dispatch [:rnrf/navigate! :home])}]
      [rnp/drawer-item {:label    "lada mięsna"
                        :on-press #(rf/dispatch [:rnrf/navigate! :home])}]]]))
