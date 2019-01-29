(ns edc-next.orders.settings.ui
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(defn settings-button []
  [rnp/app-bar-action {:icon     "settings"
                       :on-press #(rf/dispatch [:orders.settings/show-settings-dialog true])}])


(defn settings-dialog-item-checkbox [key]
  (let [show? @(rf/subscribe [:orders.settings/product-card.show? key])]
    [rnp/checkbox {:status   (if show? "checked" "unchecked")
                   :on-press #(rf/dispatch [:orders.settings/toggle-product-card-elem key])}]))


(defn settings-dialog-item [key name]
  [rnp/list-item {:title         name
                  :right         (fn [] (r/as-element [settings-dialog-item-checkbox key]))
                  :on-press      #(rf/dispatch [:orders.settings/toggle-product-card-elem key])
                  :on-long-press #(rn/alert nil
                                            (str "pokazuje na karcie towaru " name))}])


(defn settings-dialog []
  (let [show? @(rf/subscribe [:orders.settings/show-settings-dialog?])
        pace-period @(rf/subscribe [:orders.settings/pace-period])
        card-columns @(rf/subscribe [:orders.settings/card-columns])]
    [rnp/portal
     [rnp/dialog {:visible    show?
                  :on-dismiss #(rf/dispatch [:orders.settings/show-settings-dialog false])}
      [rnp/dialog-title "ustawienia"]
      [rnp/dialog-scroll-area {:height "75%"}
       [rn/scroll-view
        [rnp/list-section {:title "długość okresu"}]
        [rnp/radio-button-group {:value           pace-period
                                 :on-value-change #(rf/dispatch [:orders.settings/set-pace-period %])}
         [rnp/list-item {:title         "dzień"
                         :right         (fn [] (r/as-element [rnp/radio-button {:value 1}]))
                         :on-press      (fn [])
                         :on-long-press #(rn/alert nil
                                                   (str "ustawia długość okresu tempa sprzedaży "
                                                        "na jeden dzień"))}]
         [rnp/list-item {:title         "tydzień"
                         :right         (fn [] (r/as-element [rnp/radio-button {:value 7}]))
                         :on-press      (fn [])
                         :on-long-press #(rn/alert nil
                                                   (str "ustawia długość okresu tempa sprzedaży "
                                                        "na jeden tydzień"))}]
         [rnp/list-item {:title         "miesiąc"
                         :right         (fn [] (r/as-element [rnp/radio-button {:value 30}]))
                         :on-press      (fn [])
                         :on-long-press #(rn/alert nil
                                                   (str "ustawia długość okresu tempa sprzedaży "
                                                        "na jeden miesiąc"))}]
         [rnp/list-item {:title         "kwartał"
                         :right         (fn [] (r/as-element [rnp/radio-button {:value 120}]))
                         :on-press      (fn [])
                         :on-long-press #(rn/alert nil
                                                   (str "ustawia długość okresu tempa sprzedaży "
                                                        "na jeden kwartał"))}]
         [rnp/list-item {:title         "rok"
                         :right         (fn [] (r/as-element [rnp/radio-button {:value 365}]))
                         :on-press      (fn [])
                         :on-long-press #(rn/alert nil
                                                   (str "ustawia długość okresu tempa sprzedaży "
                                                        "na jeden rok"))}]]
        [rnp/list-section {:title "ilość kolumn w karcie towaru"}]
        [rnp/radio-button-group {:value           card-columns
                                 :on-value-change #(rf/dispatch [:orders.settings/set-card-columns %])}
         [rnp/list-item {:title         "jedna"
                         :right         (fn [] (r/as-element
                                                 [rnp/radio-button {:value 1}]))
                         :on-press      (fn [])
                         :on-long-press #(rn/alert nil
                                                   (str "zmienia na jedną ilość kolumn "
                                                        "wyświetlanych na karcie towaru"))}]
         [rnp/list-item {:title         "dwie"
                         :right         (fn [] (r/as-element
                                                 [rnp/radio-button {:value 2}]))
                         :on-press      (fn [])
                         :on-long-press #(rn/alert nil
                                                   (str "zmienia na dwie ilość kolumn "
                                                        "wyświetlanych na karcie towaru"))}]
         [rnp/list-item {:title         "trzy"
                         :right         (fn [] (r/as-element
                                                 [rnp/radio-button {:value 3}]))
                         :on-press      (fn [])
                         :on-long-press #(rn/alert nil
                                                   (str "zmienia na trzy ilość kolumn "
                                                        "wyświetlanych na karcie towaru"))}]]
        [rnp/list-section {:title "ustawienia karty towaru"}]
        [settings-dialog-item :stock "stan towaru"]
        [settings-dialog-item :optimal "stan optymalny"]
        [settings-dialog-item :missing "brak/nadstan"]
        [settings-dialog-item :volume "sprzedana ilość j/m"]
        [settings-dialog-item :sales "sprzedaż za okres"]
        [settings-dialog-item :profit "zysk za okres"]
        [settings-dialog-item :pace "tempo sprzedaży"]
        [settings-dialog-item :margin "średnia marża"]
        [settings-dialog-item :established-margin "założona marża"]
        ;[settings-dialog-item :rank "ranking"]
        [settings-dialog-item :price "cena sprzedaży"]
        [settings-dialog-item :buy-price "cena zakupu ec"]
        [settings-dialog-item :cg-price "cena zakupu cg"]
        [settings-dialog-item :last-sale "data ostaniej sprzedaży"]
        [settings-dialog-item :last-delivery "data ostaniej dostawy"]
        [settings-dialog-item :promotion "nazwa promocji"]]]
      [rnp/dialog-actions
       [rnp/button {:on-press #(rf/dispatch [:orders.settings/show-settings-dialog false])}
        "ok"]]]]))
