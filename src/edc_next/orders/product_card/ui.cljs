(ns edc-next.orders.product-card.ui
  (:require [reagent.core :as r]
            [reagent.ratom :refer [reaction]]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [oops.core :refer [oget ocall+]]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(defn product-title [{:keys [name]}]
  [rnp/title {:number-of-lines    1
              :allow-font-scaling false}
   name])


(defn product-subheading [{:keys [ean place]}]
  [rn/view {:style {:flex-direction  :row
                    :justify-content :space-between}}
   [rnp/subheading {:number-of-lines    1
                    :allow-font-scaling false
                    :on-long-press      #(rf/dispatch [:frisco/search! ean])}
    ean]
   [rnp/subheading {:number-of-lines    1
                    :allow-font-scaling false}
    (or place "brak miejsca")]])


(defn supply-input-dialog [{stock :qty :keys [pace]} qty show?]
  [rnp/portal
   [rnp/dialog {:visible    @show?
                :on-dismiss #(reset! show? false)}
    [rnp/dialog-title "wprowadź zapas"]
    [rnp/dialog-content
     [rnp/text-input {:value      (.toFixed (/ (+ (max 0 stock) qty) pace))
                      :auto-focus true}]]
    [rnp/dialog-actions
     [rnp/button {:on-press #(reset! show? false)}
      "anuluj"]]]])


(defn supply-change [product qty]
  (let [show-dialog? (r/atom false)]
    (fn [{stock :qty :keys [optimal pace] :as product} qty]
      (let [accent @(rf/subscribe [:theme/get :colors :accent])
            doc-id @(rf/subscribe [:orders/selected-document.id])
            missing-qty (- optimal (max 0 stock))]
        [rn/view {:style {:flex 1}}
         ;[supply-input-dialog product qty show-dialog?]
         [rn/view {:style {:flex-direction  :row
                           :justify-content :center}}
          [rnp/headline "zapas na"]]
         [rn/view {:style {:flex-direction  :row
                           :justify-content :space-between
                           :align-items     :center}}
          (when (pos? pace)
            [rnp/icon-button {:icon          "remove"
                              :color         accent
                              :on-press      #(if doc-id
                                                (rf/dispatch [:orders/document.supply-days.dec doc-id product qty])
                                                (rf/dispatch [:orders/show-documents-dialog true]))
                              :on-long-press #(if doc-id
                                                (rf/dispatch [:orders/change-document.qty doc-id product 0])
                                                (rf/dispatch [:orders/show-documents-dialog true]))
                              :style         {:margin 8}}])
          [rnp/touchable-ripple {:on-press #(reset! show-dialog? true)
                                 :style    {:flex        1
                                            :align-items :center}}
           [rnp/text {:style {:font-size 24}}
            (if (pos? pace)
              (.toFixed (/ (+ (max 0 stock) qty) pace))
              "wieczność")]]
          (when (pos? pace)
            [rnp/icon-button {:icon          "add"
                              :color         accent
                              :on-press      #(if doc-id
                                                (rf/dispatch [:orders/document.supply-days.inc doc-id product qty])
                                                (rf/dispatch [:orders/show-documents-dialog true]))
                              :on-long-press #(if doc-id
                                                (rf/dispatch [:orders/change-document.qty
                                                              doc-id product missing-qty])
                                                (rf/dispatch [:orders/show-documents-dialog true]))
                              :style         {:margin 8}}])]]))))


(defn submit-qty-change [value doc-id product]
  (let [value* (if (not= value "") value 0)
        val (js/parseFloat value*)]
    (rf/dispatch-sync [:orders/change-document.qty
                       doc-id product val])))



(defn qty-input-dialog [doc-id product qty show?]
  (let [tmp-val (atom (str qty))]
    [rnp/portal
     [rnp/dialog {:visible    @show?
                  :on-dismiss #(reset! show? false)}
      [rnp/dialog-title "wprowadź ilość"]
      [rnp/dialog-content
       [rnp/text-input {:default-value     (if (pos? qty) (str qty) "")
                        :auto-focus        true
                        :keyboard-type     :numeric
                        :on-change-text    #(when (or (= "" %) (re-find #"^[0-9]*$" %))
                                              (reset! tmp-val %))
                        :on-submit-editing (fn []
                                             (submit-qty-change @tmp-val doc-id product)
                                             (reset! show? false))
                        }]]
      [rnp/dialog-actions
       [rnp/button {:on-press (fn []
                                (submit-qty-change @tmp-val doc-id product)
                                (reset! show? false))}
        "ok"]]]]))


(defn qty-change [product qty]
  (let [show-dialog? (r/atom false)]
    (fn [{stock :qty :keys [optimal] :as product} qty]
      (let [accent @(rf/subscribe [:theme/get :colors :accent])
            doc-id @(rf/subscribe [:orders/selected-document.id])
            missing-qty (- optimal (max 0 stock))]
        [rn/view {:style {:flex 1}}
         [qty-input-dialog doc-id product qty show-dialog?]
         [rn/view {:style {:flex-direction  :row
                           :justify-content :center}}
          [rnp/headline "ilość"]]
         [rn/view {:style {:flex-direction  :row
                           :justify-content :space-between
                           :align-items     :center}}
          [rnp/icon-button {:icon          "remove"
                            :color         accent
                            :on-press      #(if doc-id
                                              (rf/dispatch [:orders/change-document.qty
                                                            doc-id product (max 0 (dec qty))])
                                              (rf/dispatch [:orders/show-documents-dialog true]))
                            :on-long-press #(if doc-id
                                              (rf/dispatch [:orders/change-document.qty doc-id product 0])
                                              (rf/dispatch [:orders/show-documents-dialog true]))
                            :style         {:margin 8}}]
          [rnp/touchable-ripple {:on-press #(if doc-id
                                              (reset! show-dialog? true)
                                              (rf/dispatch [:orders/show-documents-dialog true]))
                                 :style    {:flex        1
                                            :align-items :center}}
           [rnp/text {:style {:font-size 24}}
            (.toFixed qty)]]
          [rnp/icon-button {:icon          "add"
                            :color         accent
                            :on-press      #(if doc-id
                                              (rf/dispatch [:orders/change-document.qty
                                                            doc-id product (inc qty)])
                                              (rf/dispatch [:orders/show-documents-dialog true]))
                            :on-long-press #(if doc-id
                                              (rf/dispatch [:orders/change-document.qty doc-id product missing-qty])
                                              (rf/dispatch [:orders/show-documents-dialog true]))
                            :style         {:margin 8}}]]]))))


(defn basic-info [name value]
  [rn/view {:style {:flex            1
                    :flex-direction  :row
                    :justify-content :space-between}}
   [rnp/text {:number-of-lines 1}
    name]
   [rn/view {:style {:align-items :flex-end}}
    [rnp/text value]]])


(defn pace-info [{:keys [pace] :as product}]
  (let [pace-period @(rf/subscribe [:orders.settings/pace-period])]
    [basic-info "tempo" (.toFixed (* pace pace-period) 2)]))


(defn margin-info [{:keys [margin]}]
  [basic-info "średnia marża" (str (.toFixed (* margin 100) 2) "%")])


(defn established-margin-info [{:keys [established-margin]}]
  [basic-info "założona marża" (str (.toFixed (* established-margin 100) 2) "%")])


(defn stock-info [{:keys [stock]}]
  [basic-info "stan" (.toFixed stock 2)])


(defn missing-info [{:keys [optimal stock]}]
  (let [missing-qty (- optimal (max 0 stock))]
    [basic-info
     (if (pos? missing-qty) "brakuje" "nadmiar")
     (.toFixed (e/abs missing-qty) 2)]))


(defn volume-info [{:keys [volume]}]
  [basic-info "sprzedaż w j/m" (.toFixed volume 2)])


(defn sales-info [{:keys [sales pace]}]
  [basic-info "obrót" (.toFixed (* sales pace 30) 2)])


(defn profit-info [{:keys [profit]}]
  [basic-info "zysk" (.toFixed profit 2)])


(defn last-delivery-info [{:keys [last-delivery]}]
  [basic-info "ost. zak." last-delivery])


(defn last-sale-info [{:keys [last-sales]}]
  [basic-info "ost. sprzed." last-sales])


(defn optimal-qty-info [{:keys [optimal]}]
  [basic-info "optymalny" (.toFixed optimal 2)])


(defn price-info [{:keys [price]}]
  [basic-info "cena" (str (.toFixed price 2) "zł")])


(defn buy-price-info [{:keys [buy-price]}]
  [basic-info "zakup netto" (str (.toFixed buy-price 2) "zł")])


(defn cg-price-info [{:keys [ean]}]
  (let [cg-price @(rf/subscribe [:cg-warehouse/product.price-2 ean])]
    [basic-info "cg netto"
     (if cg-price
       (str (.toFixed cg-price 2) "zł")
       "brak")]))


(defn promotion-info [{:keys [promotion]}]
  [basic-info "promocja" (or promotion "brak")])


;(defn rank-info [level {:keys [category-id sales]}]
;  (let [cat (e/get-substr category-id 0 (* level 2))
;        cat-name (if (seq cat)
;                   @(rf/subscribe [:orders/category-name cat])
;                   "globalny")
;        rank @(rf/subscribe [:orders/percentile-rank.pace cat sales])]
;    [rn/view {:style {:flex            1
;                      :flex-direction  :row
;                      :justify-content :space-between}}
;     [rnp/text {:number-of-lines 1
;                :style           {:flex 2}}
;      (str "ranking " cat-name)]
;     [rn/view {:style {:flex        1
;                       :align-items :flex-end}}
;      [rnp/text (str (.toFixed (* rank 100) 2) "%")]]]))


(defn card-line [items]
  (into [rn/view {:style {:flex-direction :row}}]
        (interpose
          [rn/view {:style {:width 16}}]
          items)))


(defn card-info [& items]
  (let [card-columns @(rf/subscribe [:orders.settings/card-columns])
        n (count items)
        nil-item [rn/view {:style {:flex 1}}]
        items* (into (vec (filter identity items))
                     (repeat (- card-columns (mod n card-columns)) nil-item))]
    [rn/view
     (doall
       (for [[i line] (map vector (range) (partition-all card-columns items*))]
         ^{:key i}
         [card-line line]))]))


(defn card [{:keys [ean] :as product}]
  (let [qty @(rf/subscribe [:orders/selected-document.qty ean])]
    [rnp/card {:style {:flex         1
                       :align-self   :stretch
                       :margin-top   16
                       :margin-left  16
                       :margin-right 16}}
     [rnp/card-content {:style {:flex       1
                                :align-self :stretch
                                :padding    64}}
      [product-title product]
      [product-subheading product]
      [rn/view {:style {:margin-top 16}}
       [card-info
        (when @(rf/subscribe [:orders.settings/product-card.show? :stock]) [stock-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :optimal]) [optimal-qty-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :missing]) [missing-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :volume]) [volume-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :sales]) [sales-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :profit]) [profit-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :buy-price]) [buy-price-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :pace]) [pace-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :cg-price]) [cg-price-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :margin]) [margin-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :price]) [price-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :established-margin]) [established-margin-info product])
        ;(when @(rf/subscribe [:orders/product-card.show? :rank]) [rank-info 2 product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :last-delivery]) [last-delivery-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :last-sale]) [last-sale-info product])
        (when @(rf/subscribe [:orders.settings/product-card.show? :promotion]) [promotion-info product])
        ]]
      [rn/view {:style {:flex-direction  :row
                        :justify-content :space-between
                        :margin-top      24}}
       [supply-change product qty]
       [qty-change product qty]]
      ;[rn/view {:style {:flex-direction  :row
      ;                  :justify-content :center}}
      ; [rnp/icon-button {:icon     (if-not @expand? "expand-more" "expand-less")
      ;                   :on-press #(if-not @expand?
      ;                                (reset! expand? true)
      ;                                (reset! expand? false))}]]
      ]]))
