(ns edc-next.start-screen.ui
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.server.ui :as server.ui]
            [edc-next.snackbar.ui :as snackbar.ui]))


(defn start-screen-bar []
  (let [theme @(rf/subscribe [:theme/get])]
    [rnp/app-bar-header {:theme theme}
     [rnp/app-bar-content {:title    "edc"
                           :subtitle "brak połączenia"
                           ;:style    {:margin-left 56}
                           }]
     [server.ui/connection-status-button]]))


(defn view []
  (let [theme @(rf/subscribe [:theme/get])
        servers @(rf/subscribe [:server/servers.by-id])]
    [rnp/provider {:theme theme}
     [server.ui/new-server-dialog]
     [rn/view {:style {:flex            1
                       :justify-content :center
                       :align-items     :center}}
      (doall
        (for [[id {:keys [name host]}] servers]
          ^{:key id}
          [rnp/card {:style         {:width  "50%"
                                     :height 100
                                     :margin 16}
                     :on-press      #(rf/dispatch [:server/connect id])
                     :on-long-press #(rf/dispatch [:server/delete-server id])}
           [rnp/card-content
            [rnp/title {:style {:text-align :center}} name]
            [rnp/paragraph {:style {:text-align :center}} host]]]))
      [rnp/button {:on-press (fn [_]
                               (rf/dispatch-sync [:server/show-new-server-dialog true])
                               (r/flush))}
       "dodaj"]
      [snackbar.ui/snackbar]]]))