(ns edc-next.camera.db
  (:require [oops.core :refer [oget]]
            [edc-next.camera.core :as camera]))

(def state {:camera {:_flash-mode       camera/flash-mode-off
                     :_fab-open?        false
                     :zoom              0
                     :_show-preview?    false
                     :barcode-types     [camera/barcode-type-ean13
                                         camera/barcode-type-ean8]
                     :_barcode-detected nil}})
