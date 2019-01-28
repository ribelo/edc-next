(ns edc-next.camera.core
  (:require [reagent.core :as r]
            [oops.core :refer [oget]]
            [edc-next.expo.core :as expo]))

(def flash-mode-off (oget expo/expo "Camera.Constants.FlashMode.off"))
(def flash-mode-on (oget expo/expo "Camera.Constants.FlashMode.on"))
(def flash-mode-torch (oget expo/expo "Camera.Constants.FlashMode.torch"))
(def flash-mode-auto (oget expo/expo "Camera.Constants.FlashMode.auto"))
(def back-camera (oget expo/expo "Camera.Constants.Type.back"))
(def front-camera (oget expo/expo "Camera.Constants.Type.front"))
(def barcode-type-ean13 (oget expo/expo "BarCodeScanner.Constants.BarCodeType.ean13"))
(def barcode-type-ean8 (oget expo/expo "BarCodeScanner.Constants.BarCodeType.ean8"))


(def camera (r/adapt-react-class (oget expo/expo "Camera")))
(def barcode-scanner (r/adapt-react-class (oget expo/expo "BarCodeScanner")))