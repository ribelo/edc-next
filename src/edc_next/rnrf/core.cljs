(ns edc-next.rnrf.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [oops.core :refer [oget oget+ ocall+]]))


(def rnrf (js/require "react-native-router-flux"))


(defn get-class [class]
  (r/adapt-react-class (oget+ rnrf class)))


(def actions (oget+ rnrf :Actions))
(defn action!
  ([k]
   (ocall+ actions k))
  ([k props]
   (ocall+ actions k (clj->js props))))


(def scene (get-class :Scene))
(def router (get-class :Router))
(def reducer (oget+ rnrf :Reducer))
(def actionconst (get-class :ActionConst))
(def overlay (get-class :Overlay))
(def tabs (get-class :Tabs))
(def modal (get-class :Modal))
(def drawer (get-class :Drawer))
(def stack (get-class :Stack))
(def lightbox (get-class :Lightbox))


(defn create-reducer [params]
  (let [defaultReducer (reducer params)]
    (fn [state action]
      (println "ACTION:" action)
      (defaultReducer state action))))
