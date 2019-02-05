(ns edc-next.expo.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [oops.core :refer [ocall]]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.expo.core :as expo]))


(rf/reg-event-fx
  :expo.util/reload
  (fn [_ _]
    {:expo.util/reload true}))
