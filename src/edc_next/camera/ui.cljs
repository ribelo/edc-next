(ns edc-next.camera.ui
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [oops.core :refer [oget]]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.expo.core :as expo]
            [edc-next.camera.core :as camera]))


(defn barcode-scanner-fab []
  (fn []
    [rnp/fab {:icon     "camera"
              :on-press #(rf/dispatch [:camera/show-preview])
              :style    {:position :absolute
                         :right    32
                         :bottom   32}}]))
