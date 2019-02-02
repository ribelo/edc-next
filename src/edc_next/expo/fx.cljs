(ns edc-next.expo.fx
  (:require [re-frame.core :as rf]
            [taoensso.encore :as e]
            [oops.core :refer [ocall]]
            [edc-next.expo.core :as expo]))


(rf/reg-fx
  :expo/open-browser
  (fn [url]
    (ocall expo/web-browser "openBrowserAsync" url)))


(rf/reg-fx
  :expo/present-local-notification
  (fn [notification]
    (ocall expo/notifications "presentLocalNotificationAsync"
           notification)))


(rf/reg-fx
  :expo/speek
  (fn
    ([text opts]
     (ocall expo/speech "speek" text (clj->js opts)))
    ([text]
     (ocall expo/speech "speek" text))))
