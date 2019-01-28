(ns edc-next.expo.core
  (:require [reagent.core :as r]
            [oops.core :refer [oget oget+]]))


(def expo (js/require "expo"))
(def AtExpo (js/require "@expo/vector-icons"))
(def ion-icon (r/adapt-react-class (oget AtExpo "Ionicons")))
(def material-icon (r/adapt-react-class (oget AtExpo "MaterialIcons")))
(defn material-icon* [name]
  (fn [props]
    (let [size (oget props :size)
          color (oget props :color)]
      (r/as-element
        [material-icon {:name  name
                        :size  size
                        :color color}]))))

(def bar-code-scanner (oget expo "BarCodeScanner"))

(def camera (oget expo "Camera"))

(def constants (oget expo "Constants"))
(def status-bar-height (oget constants "statusBarHeight"))

(def permissions (oget expo "Permissions"))

(oget expo "BarCodeScanner" "Constants" "BarCodeType")


