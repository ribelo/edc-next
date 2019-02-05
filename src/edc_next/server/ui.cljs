(ns edc-next.server.ui
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [oops.core :refer [ocall]]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.server.events]
            [edc-next.server.subs]))


(defn connection-status-button []
  (let [data-loading? (rf/subscribe [:server/connecting?])
        ws-open? (rf/subscribe [:ws/open?])]
    (fn []
      [rnp/app-bar-action {:icon          (if @ws-open? "cloud-done" "cloud-queue")
                           :color         (rnp/color :black)
                           :on-press      #(rf/dispatch [:server/show-disconnect-dialog true])
                           :on-long-press (fn []
                                            (rf/dispatch [:do
                                                          [:ui/show-snackbar
                                                           "wyczyszczono cache!"]
                                                          [:db/clear-async-storage!]
                                                          [:expo.util/reload]]))}])))


(defn disconnect-dialog []
  (let [show? @(rf/subscribe [:server/show-disconnect-dialog?])
        {:keys [id] :as server} @(rf/subscribe [:server/connected-server])]
    [rnp/portal
     [rnp/dialog {:visible    show?
                  :on-dismiss #(rf/dispatch [:server/show-disconnect-dialog false])}
      [rnp/dialog-title (str "zerwać połącznienie z serwerem " (:name server) "?")]
      [rnp/dialog-actions
       [rnp/button {:on-press #(rf/dispatch [:server/disconnect id])}
        "rozłącz"]]]]))


(defn new-server-dialog []
  (let [market-id (r/atom "")
        server-name (r/atom "")
        server-host (r/atom "")
        name-error? (r/atom false)
        host-error? (r/atom false)]
    (fn []
      (let [show? @(rf/subscribe [:server/show-new-server-dialog?])]
        [rnp/portal
         [rnp/dialog {:visible    show?
                      :on-dismiss (fn [_]
                                    (rf/dispatch-sync [:server/show-new-server-dialog false])
                                    (r/flush))}
          [rnp/dialog-title "nowy serwer"]
          [rnp/dialog-content
           [rnp/text-input {:placeholder  "market id"
                            :value        @market-id
                            :onChangeText (fn [text]
                                            (reset! market-id text)
                                            (r/flush))
                            :style        {:margin-bottom    16
                                           :background-color :white}}]
           [rnp/text-input {:placeholder  "nazwa"
                            :label        (when @name-error? "nazwa nie może być pusta")
                            :error        @name-error?
                            :value        @server-name
                            :onChangeText (fn [text]
                                            (reset! name-error? (empty? text))
                                            (reset! server-name text)
                                            (r/flush))
                            :style        {:margin-bottom    16
                                           :background-color :white}}]
           [rnp/text-input {:placeholder    "adres"
                            :label          (when @host-error? "adres nie może być pusty")
                            :error          @host-error?
                            :value          @server-host
                            :on-change-text (fn [text]
                                              (reset! host-error? (empty? text))
                                              (reset! server-host text)
                                              (r/flush))
                            :style          {:margin-bottom    16
                                             :background-color :white}}]]
          [rnp/dialog-actions
           [rnp/button {:on-press (fn []
                                    (reset! name-error? (empty? @server-name))
                                    (reset! host-error? (empty? @server-host))
                                    (cond
                                      (= "debug" @server-name)
                                      (do (rf/dispatch [:server/create-new-server "debug-dom" "debug-dom" "192.168.8.62:3000"])
                                          (rf/dispatch [:server/create-new-server "debug-firma" "debug-firma" "10.0.0.161:3000"]))
                                      (and (seq @server-name) (seq @server-host))
                                      (rf/dispatch [:server/create-new-server @market-id @server-name @server-host])))}
            "dodaj"]]]]))))
