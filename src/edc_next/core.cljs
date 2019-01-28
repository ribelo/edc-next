(ns edc-next.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
            [taoensso.encore :as e]
            [taoensso.timbre :as timbre]
            [oops.core :refer [ocall]]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]
            [edc-next.rnrf.core :as rnrf]
            [edc-next.init]
            [edc-next.ui.core :as ui.core]
            [edc-next.start-screen.ui :as start-screen.ui]
            [edc-next.drawer.ui :as drawer.ui]
            [edc-next.ec-orders.ui :as ec-orders.ui]))


(def expo (js/require "expo"))


(defn app-root []
  (let [current-scene (rf/subscribe [:rnrf/current-scene])]
    (r/create-class
      {
       :component-will-mount
       (fn []
         (when (and goog/DEBUG @current-scene) (rf/dispatch [:rnrf/replace! @current-scene])))
       :reagent-render
       (fn []
         [rnrf/router {:onStateChange (fn [_] (rf/dispatch [:rnrf/refresh-scene]))}
          [rnrf/stack {:key "root"}
           [rnrf/scene {:key       "start-screen"
                        :component (r/reactify-component start-screen.ui/view)
                        :nav-bar   (r/reactify-component start-screen.ui/start-screen-bar)}]
           [rnrf/stack {:key        "app"
                        :hideNavBar true}
            [rnrf/drawer {:key               "drawer"
                          :position          :left
                          :hideNavBar        true
                          :content-component (r/reactify-component drawer.ui/drawer)}
             [rnrf/scene {:key       "ec-orders"
                          :component (r/reactify-component ec-orders.ui/view)
                          :nav-bar   (r/reactify-component ec-orders.ui/header-bar)}]]]]]
         )})))



(defn init []
  (rf/dispatch-sync [:edc-next/boot])
  (ocall expo "registerRootComponent" (r/reactify-component app-root)))

