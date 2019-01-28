(ns edc-next.rnp.core
  (:require [reagent.core :as r]
            [taoensso.encore :as e]
            [oops.core :refer [oget+ ocall+]]))


(def rnp (js/require "react-native-paper"))


(defn get-class [& class]
  (r/adapt-react-class (oget+ rnp class)))


(def colors (oget+ rnp :Colors))


(defn color [color]
  (oget+ colors color))


(def default-theme (js->clj (oget+ rnp :DefaultTheme) :keywordize-keys true))


(defn theme [style]
  (clj->js (e/nested-merge default-theme style)))


;(def with-theme (get-class :withTheme))

(defn with-theme [component]
  (let [hoc (oget+ rnp :withTheme)]
    (r/adapt-react-class (hoc (r/reactify-component component)))))


(def provider (get-class :Provider))
(def app-bar (get-class :Appbar))
(def app-bar-action (get-class :Appbar :Action))
(def app-bar-back-action (get-class :Appbar :BackAction))
(def app-bar-content (get-class :Appbar :Content))
(def app-bar-header (get-class :Appbar :Header))
(def banner (get-class :Banner))
(def bottom-navigation (get-class :BottomNavigation))
(def button (get-class :Button))
(def caption (get-class :Caption))
(def card (get-class :Card))
(def card-actions (get-class :Card :Actions))
(def card-content (get-class :Card :Content))
(def card-cover (get-class :Card :Cover))
(def checkbox (get-class :Checkbox))
(def dialog (get-class :Dialog))
(def dialog-actions (get-class :Dialog :Actions))
(def dialog-content (get-class :Dialog :Content))
(def dialog-scroll-area (get-class :Dialog :ScrollArea))
(def dialog-title (get-class :Dialog :Title))
(def divider (get-class :Divider))
(def drawer (get-class :Drawer))
(def drawer-item (get-class :Drawer :Item))
(def drawer-section (get-class :Drawer :Section))
(def fab (get-class :FAB))
(def fab-group (get-class :FAB :Group))
(def headline (get-class :Headline))
(def helper-text (get-class :HelperText))
(def icon-button (get-class :IconButton))
(def list (get-class :List))
(def list-accordion (get-class :List :Accordion))
(def list-item (get-class :List :Item))
(def list-icon (get-class :List :Icon))
(def list-section (get-class :List :Section))
(def modal (get-class :Modal))
(def paragraph (get-class :Paragraph))
(def portal (get-class :Portal))
(def portal-host (get-class :Portal :Host))
(def progress-bar (get-class :ProgressBar))
(def radio-button (get-class :RadioButton))
(def radio-button-android (get-class :RadioButton :Android))
(def radio-button-ios (get-class :RadioButton :IOS))
(def radio-button-group (get-class :RadioButton :Group))
(def search-bar (get-class :Searchbar))
(def snack-bar (get-class :Snackbar))
(def subheading (get-class :Subheading))
(def surface (get-class :Surface))
(def switch (get-class :Switch))
(def text (get-class :Text))
(def text-input (get-class :TextInput))
(def title (get-class :Title))
(def touchable-ripple (get-class :TouchableRipple))



