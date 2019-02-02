(ns edc-next.expo.core
  (:require [reagent.core :as r]
            [oops.core :refer [oget oget+ ocall]]))


(def expo (js/require "expo"))
(def AtExpo (js/require "@expo/vector-icons"))
(def ion-icon (r/adapt-react-class (oget AtExpo "Ionicons")))
(def material-icons (r/adapt-react-class (oget AtExpo "MaterialIcons")))
(def material-community-icons (r/adapt-react-class (oget AtExpo "MaterialCommunityIcons")))


(defn material-icon [name]
  (fn [props]
    (let [size (oget props :size)
          color (oget props :color)]
      (r/as-element
        [material-icons {:name name
                        :size  size
                        :color color}]))))


(defn material-community-icon [name]
  (fn [props]
    (let [size (oget props :size)
          color (oget props :color)]
      (r/as-element
        [material-community-icons {:name  name
                         :size  size
                         :color color}]))))



(def bar-code-scanner (oget expo "BarCodeScanner"))

(def camera (oget expo "Camera"))

(def constants (oget expo "Constants"))
(def status-bar-height (oget constants "statusBarHeight"))

(def permissions (oget expo "Permissions"))

(def web-browser (oget expo "WebBrowser"))
;Notifications.presentLocalNotificationAsync

(def notifications (oget expo "Notifications"))

(def speech (oget expo "Speech"))




