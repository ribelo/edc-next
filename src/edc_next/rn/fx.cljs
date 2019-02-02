(ns edc-next.rn.fx
  (:require [re-frame.core :as rf]
            [taoensso.encore :as e]
            [oops.core :refer [ocall]]
            [edc-next.rn.core :as rn]))


(rf/reg-fx
  :rn/vibrate
  (fn [pattern]
    (rn/vibrate (clj->js pattern))))


(rf/reg-fx
  :rn/keyboard-dismiss
  (fn [val]
    (when val (ocall rn/keyboard "dismiss"))))
