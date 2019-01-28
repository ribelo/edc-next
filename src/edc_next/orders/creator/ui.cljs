(ns edc-next.ec-orders.creator.ui
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(defn create-order-button []
  (let [doc-id @(rf/subscribe [:ec-orders/selected-document.id])]
    [rnp/app-bar-action {:icon          "create"
                         :on-press      (fn []
                                          (if doc-id
                                            (rf/dispatch [:ec-orders.creator/show-make-order-dialog true])
                                            (rf/dispatch [:ec-orders/show-documents-dialog true])))
                         :on-long-press #(rn/alert
                                           "generowanie zamówienie"
                                           (str "tworzenie zamówienia na podstawie rotacji, "
                                                "preferowany towar od dostawcy eurocash"))}]))


(defn min-margin-input []
  (let [primary @(rf/subscribe [:theme/get :colors :primary])
        min-margin @(rf/subscribe [:ec-orders.creator/min-margin])]
    [rn/view {:style {:flex-direction  :row
                      :justify-content :space-between
                      :align-items     :center
                      :width           120}}
     [rnp/icon-button {:icon     "remove"
                       :size     16
                       :color    primary
                       :on-press #(rf/dispatch [:ec-orders.creator/dec-min-margin])}]
     [rnp/text (str (.toFixed (* 100 min-margin) 0) "%")]
     [rnp/icon-button {:icon     "add"
                       :size     16
                       :color    primary
                       :on-press #(rf/dispatch [:ec-orders.creator/inc-min-margin])}]]))


(defn min-margin-item []
  [rnp/list-item {:title         "minimalna marża"
                  :right         (fn [] (r/as-element [min-margin-input]))
                  :on-press      (fn [])
                  :on-long-press #(rn/alert
                                    "minimalna marża"
                                    (str "minimalna wysokość marży dla towarów, "
                                         "które mają być brane pod uwagę przy "
                                         "generowaniu zamówienia. "
                                         "pozwala to wyeliminować towary, "
                                         "których sprzedaż przynosi straty."))}])


(defn min-pace-input []
  (let [primary @(rf/subscribe [:theme/get :colors :primary])
        min-pace @(rf/subscribe [:ec-orders.creator/min-pace])
        pace-period @(rf/subscribe [:ec-orders.settings/pace-period])]
    [rn/view {:style {:flex-direction  :row
                      :justify-content :space-between
                      :align-items     :center
                      :width           120}}
     [rnp/icon-button {:icon     "remove"
                       :size     16
                       :color    primary
                       :on-press #(rf/dispatch [:ec-orders.creator/dec-min-pace])}]
     [rnp/text (.toFixed (* min-pace pace-period) 2)]
     [rnp/icon-button {:icon     "add"
                       :size     16
                       :color    primary
                       :on-press #(rf/dispatch [:ec-orders.creator/inc-min-pace])}]]))


(defn min-pace-item []
  (let [pace-period @(rf/subscribe [:ec-orders.settings/pace-period])
        period-name (case pace-period
                      1 "dzienne"
                      7 "tygodniowe"
                      30 "miesięczne"
                      120 "kwartalnie"
                      365 "roczne")]
    [rnp/list-item {:title         (str "minimalne tempo " period-name)
                    :right         (fn [] (r/as-element [min-pace-input]))
                    :on-press      (fn [])
                    :on-long-press #(rn/alert
                                      (str "minimalne tempo " period-name)
                                      (str "minimalna tempo sprzedaży dla towarów, "
                                           "które mają być brane pod uwagę przy "
                                           "generowaniu zamówienia. "
                                           "pozwala to wyeliminować towary, "
                                           "których tempo rotacji jest zbyt niskie."))}]))


(defn category-checkbox [id status]
  [rnp/checkbox {:status   (if status "checked" "unchecked")
                 :on-press #(rf/dispatch [:ec-orders.creator/toggle-category id])}])


(defn category-item [id category status]
  [rnp/list-item {:title         category
                  :right         (fn [] (r/as-element [category-checkbox id status]))
                  :on-press      #(rf/dispatch [:ec-orders.creator/toggle-category id])
                  :on-long-press #(rn/alert
                                    "wybór kategorii"
                                    "możesz ograniczyć generowanie do wybranych kategorii")}])


(defn category-list []
  (let [categories @(rf/subscribe [:warehouse/categories.parents])
        selected-categories @(rf/subscribe [:ec-orders.creator/selected-categories])]
    [rn/view
     [rnp/list-section {:title "wybierz kategorie"}]
     (doall
       (for [[id category] categories]
         ^{:key id}
         [category-item id category (get selected-categories id)]))]))


(defn only-below-minimum-checkbox []
  (let [only? @(rf/subscribe [:ec-orders.creator/only-below-minimum?])]
    [rnp/checkbox {:status   (if only? "checked" "unchecked")
                   :on-press #(rf/dispatch [:ec-orders.creator/set-only-below-minimum])}]))


(defn only-below-minimum-item []
  [rnp/list-item {:title         "tylko towary poniżej stanu minimalnego"
                  :right         (fn [] (r/as-element [only-below-minimum-checkbox]))
                  :on-press      #(rf/dispatch [:ec-orders.creator/set-only-below-minimum])
                  :on-long-press #(rn/alert
                                    (str "tylko towary poniżej stanu minimalnego")
                                    (str "opcja pozwala wygenerować zamówienie tylko dla towarów, "
                                         "których stan magazynowy jest niższy niż ustalony stan minimalny"))}])


(defn only-cheaper-than-cg-checkbox []
  (let [only? @(rf/subscribe [:ec-orders.creator/only-cheaper-than-cg?])]
    [rnp/checkbox {:status   (if only? "checked" "unchecked")
                   :on-press #(rf/dispatch [:ec-orders.creator/set-only-cheaper-than-cg])}]))


(defn only-cheaper-than-cg-item []
  [rnp/list-item {:title         "tylko tańsze niż w cg"
                  :right         (fn [] (r/as-element [only-cheaper-than-cg-checkbox]))
                  :on-press      #(rf/dispatch [:ec-orders.creator/set-only-cheaper-than-cg])
                  :on-long-press #(rn/alert
                                    (str "tylko towary tańsze niż w magazynie centralnym")
                                    (str "opcja pozwala wygenerować pominąć towary, "
                                         "których cena jest wyższa, niż w magazynie cg"))}])


(defn create-order-dialog []
  (let [show? @(rf/subscribe [:ec-orders.creator/show-make-order-dialog?])]
    [rnp/portal
     [rnp/dialog {:visible    show?
                  :on-dismiss #(rf/dispatch [:ec-orders.creator/show-make-order-dialog false])}
      [rnp/dialog-title "tworzenie zamówienia"]
      [rnp/dialog-scroll-area
       [rn/scroll-view {:style {:height "75%"}}
        [min-margin-item]
        [min-pace-item]
        [only-below-minimum-item]
        [only-cheaper-than-cg-item]
        [category-list]]]
      [rnp/dialog-actions
       [rnp/button {:on-press #(rf/dispatch [:ec-orders.creator/show-make-order-dialog false])}
        "anuluj"]
       [rnp/button {:on-press #(rf/dispatch [:ec-orders.creator/make-market-order])}
        "wypełnij"]]]]))


