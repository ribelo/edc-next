(ns edc-next.orders.ui
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [oops.core :refer [oget ocall]]
            [cljs-time.core :as dt]
            [cljs-time.coerce :as dtc]
            [cljs-time.format :as dtf]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.expo.core :as expo]
            [edc-next.server.ui :as server.ui]
            [edc-next.orders.product-card.ui :as product-card.ui]
            [edc-next.camera.core :as camera]
            [edc-next.camera.ui :as camera.ui]
            [edc-next.orders.creator.ui :as creator.ui]
            [edc-next.orders.settings.ui :as settings.ui]
            [edc-next.orders.import.ui :as import.ui]
            [edc-next.orders.utils :as u]
            [edc-next.snackbar.ui :as snackbar.ui])
  (:import (goog.date UtcDateTime)))


(defn documents-button []
  (let [accent @(rf/subscribe [:theme/get :colors :accent])
        doc-id @(rf/subscribe [:orders/selected-document.id])]
    [rnp/app-bar-action {:icon          (if doc-id "assignment" "assignment-late")
                         :color         (if doc-id (rnp/color :black) accent)
                         :on-press      #(rf/dispatch [:orders/show-documents-dialog true])
                         :on-long-press #(rn/alert
                                           ""
                                           "otwiera okno wyboru dokumentu")}]))


(defn show-only-ordered-button []
  (let [only-ordered? @(rf/subscribe [:orders/show-only-ordered?])
        accent @(rf/subscribe [:theme/get :colors :accent])
        doc-id @(rf/subscribe [:orders/selected-document.id])]
    [rnp/app-bar-action {:icon          (if only-ordered? "turned-in" "turned-in-not")
                         :color         (if only-ordered? accent (rnp/color :black))
                         :on-press      (fn []
                                          (if doc-id
                                            (rf/dispatch [:orders/show-only-ordered])
                                            (rf/dispatch [:orders/show-documents-dialog true])))
                         :on-long-press #(rn/alert
                                           ""
                                           (if-not only-ordered?
                                             (str "po włączeniu zostana pokazane "
                                                  "tylko towary będące na dokumencie")
                                             (str "po wyłączeniu zostana pokazane "
                                                  "wszystkie towary")))}]))


(defn export-button []
  (let [doc-id @(rf/subscribe [:orders/selected-document.id])
        document @(rf/subscribe [:orders/selected-document])
        supplier @(rf/subscribe [:orders.creator/supplier])]
    [rnp/app-bar-action {:icon          (expo/material-community-icon "export")
                         :color         (rnp/color :black)
                         :on-press      (fn []
                                          (if doc-id
                                            (case supplier
                                              "ec" (rf/dispatch [:do
                                                                 [:sync/document->collector document]
                                                                 [:snackbar/show "wysłano do ec" :ok]])
                                              "cg" (rf/dispatch [:do
                                                                 [:sync/document->ftp document]
                                                                 [:snackbar/show "wysłano do cg" :ok]]))
                                            (rf/dispatch [:orders/show-documents-dialog true])))
                         :on-long-press #(rn/alert
                                           ""
                                           (str "eksportuje dokument do " supplier))}]))


(defn import-button []
  [rnp/app-bar-action {:icon          (expo/material-community-icon "import")
                       :color         (rnp/color :black)
                       :on-press      (fn []
                                        (rf/dispatch [:orders.import/get-mm-file-list]))
                       :on-long-press #(rn/alert
                                         ""
                                         (str "importuje dokument z cg"))}])


(defn search-bar []
  (let [search-value-tmp (rf/subscribe [:orders/search-value.tmp])
        back-handler (r/atom nil)]
    (r/create-class
      {:component-did-mount
       (fn [] (reset! back-handler
                      (rn/on-back-press (fn [] (rf/dispatch [:orders/show-search-bar false]) true))))
       :component-will-unmount
       (fn [] (rn/remove-back-handler @back-handler))
       :reagent-render
       (fn []
         [rn/view
          [rn/view {:style {:height           expo/status-bar-height
                            :background-color (rnp/color :green500)}}]
          [rnp/search-bar {:value          @search-value-tmp
                           :placeholder    "nazwa lub kod kreskowy"
                           :auto-focus     true
                           ;:on-blur        #(rf/dispatch [:ui/hide-search-bar])
                           :icon           "arrow-back"
                           :on-icon-press  #(do
                                              (rf/dispatch [:orders/show-search-bar false])
                                              (when (not= "" @search-value-tmp)
                                                (rf/dispatch [:orders/set-search-value.tmp ""])))
                           :on-change-text #(do (rf/dispatch-sync [:orders/set-search-value.tmp %])
                                                (r/flush))
                           :style          {:height 56}}]])})))


(defn app-bar []
  (let [theme @(rf/subscribe [:theme/get])
        server @(rf/subscribe [:server/connected-server])
        document-name @(rf/subscribe [:orders/selected-document.name])
        supplier @(rf/subscribe [:orders.creator/supplier])]
    [rnp/app-bar-header {:theme theme}
     [rnp/app-bar-action {:icon     "menu"
                          :color    (rnp/color :black)
                          :on-press #(rf/dispatch [:rnrf/open-drawer!])}]
     [rnp/app-bar-content {:title    (:name server)
                           :subtitle (if document-name
                                       (str "kryptonim " document-name " dostawca " supplier)
                                       "nie wybrano dokumentu")
                           }]
     [rnp/app-bar-action {:icon     "search"
                          :color    (rnp/color :black)
                          :on-press (fn []
                                      (rf/dispatch [:orders/show-search-bar true])
                                      (rf/dispatch [:camera/show-preview false])
                                      )}]
     [server.ui/connection-status-button]]))


(defn header-bar []
  (let [show-search-bar? @(rf/subscribe [:orders/show-search-bar?])]
    (if-not show-search-bar?
      [app-bar]
      [search-bar])))


(defn footer-bar []
  [rnp/app-bar
   [settings.ui/settings-button]
   [show-only-ordered-button]
   [documents-button]
   [creator.ui/create-order-button]
   [import-button]
   [export-button]
   ])



(defn documents-dialog []
  (let [tmp-name (r/atom (u/random-animal))]
    (fn []
      (let [primary @(rf/subscribe [:theme/get :colors :primary])
            show? @(rf/subscribe [:orders/show-documents-dialog?])
            documents @(rf/subscribe [:orders/documents.by-id])
            selected-id @(rf/subscribe [:orders/selected-document.id])]
        [rnp/portal
         [rnp/dialog {:visible    show?
                      :on-dismiss (fn []
                                    (rf/dispatch-sync [:orders/show-documents-dialog false])
                                    (r/flush))}
          [rnp/dialog-title "wybierz dokument"]
          (when (seq documents)
            [rnp/dialog-scroll-area
             [rn/scroll-view
              (doall
                (for [[id {:keys [name time] :as doc}] documents]
                  ^{:key id}
                  [rnp/list-item {:title       name
                                  :description (dtf/unparse (dtf/formatter "YYYY-dd-MM HH:mm:ss")
                                                            (dt/to-default-time-zone (UtcDateTime. (ocall time "toDate"))))
                                  :on-press    #(rf/dispatch [:do
                                                              [:orders/select-document doc]
                                                              [:orders/show-documents-dialog false]])
                                  :right       (fn [] (r/as-element
                                                        [rnp/icon-button {:icon     "delete"
                                                                          :on-press #(rf/dispatch [:orders/remove-document doc])}]))
                                  :left        (fn [] (r/as-element
                                                        [rnp/checkbox {:status   (if (= id selected-id)
                                                                                   "checked" "unchecked")
                                                                       :on-press #(rf/dispatch [:do
                                                                                                [:orders/select-document doc]
                                                                                                [:orders/show-documents-dialog false]])}]))}]))]])
          [rnp/dialog-content
           [rn/view {:style {:align-items :center}}
            [rnp/text-input {:value          @tmp-name
                             :on-change-text #(do (reset! tmp-name %)
                                                  (r/flush))
                             :style          {:width            150
                                              :background-color :white
                                              :align-items      :center}}]
            [rnp/icon-button {:icon     "add"
                              :color    primary
                              :on-press (fn []
                                          (rf/dispatch [:orders/new-document @tmp-name])
                                          (reset! tmp-name (u/random-animal)))}]]]]]))))


(defn products-view []
  (let [products @(rf/subscribe [:orders/view])
        data-loading? @(rf/subscribe [:orders/data-loading?])]
    [rn/view {:style {:flex 1}}
     (if-not data-loading?
       (if (seq products)
         [rn/virtualized-list {:data                    (into-array products)
                               :remove-clipped-subviews true
                               :max-to-render-per-batch 5
                               :initial-num-to-render   5
                               :window-size             11
                               :get-item                (fn [data i] (aget data i))
                               :get-item-count          (constantly (count products))
                               ;:getItemLayout           (fn [data index]
                               ;                           (println index (* index 300))
                               ;                           #js {:length 300
                               ;                                :offset (* index 300)
                               ;                                :index  index})
                               :key-extractor           (fn [item _] item)
                               :render-item             (fn [elem]
                                                          (let [ean (oget elem :item)]
                                                            (r/as-element
                                                              (let [product @(rf/subscribe [:warehouse/product ean])]
                                                                (when product
                                                                  [product-card.ui/card product])))))}]
         [rn/view {:style {:flex            1
                           :align-items     :center
                           :justify-content :center}}
          [rnp/headline "nie znaleziono :("]])
       [rn/view {:style {:flex            1
                         :align-items     :center
                         :justify-content :center}}
        [rn/activity-indicator {:animating true
                                :color     :black
                                :size      :large}]])]))


(defn barcode-scanner []
  (let [back-handler (r/atom false)]
    (r/create-class
      {:component-did-mount
       (fn [] (reset! back-handler
                      (rn/on-back-press (fn [] (rf/dispatch [:camera/show-preview false]) true))))
       :component-will-unmount
       (fn [] (rn/remove-back-handler @back-handler))
       :reagent-render
       (fn []
         (let [barcode-types @(rf/subscribe [:camera/barcode-types])]
           [rn/view {:style {:flex 1}}
            [camera/barcode-scanner {:style            {:flex       1
                                                        :align-self :stretch}
                                     :bar-code-types   barcode-types
                                     :onBarCodeScanned #(rf/dispatch [:orders/barcode-detected %])}]]))})))


(defn view []
  (let [theme @(rf/subscribe [:theme/get])
        show-camera @(rf/subscribe [:camera/show-preview?])]
    [rnp/provider {:theme theme}
     [rn/view {:style {:flex 1}}
      [documents-dialog]
      [server.ui/disconnect-dialog]
      [creator.ui/create-order-dialog]
      [settings.ui/settings-dialog]
      [import.ui/mm-list-dialog]
      (if-not show-camera
        [rn/view {:style {:flex 1}}
         [products-view]
         [footer-bar]]
        [barcode-scanner])
      [camera.ui/barcode-scanner-fab]
      [snackbar.ui/snackbar]]]))


(comment
  @(rf/subscribe [:camera/show-preview?])
  (rf/dispatch [:snackbar/show "laksdjaj"])
  )