(ns ^:figwheel-no-load env.expo.main
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [edc-next.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [env.dev]))

(enable-console-print!)

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/app-root])
(def root-el (r/as-element [reloader]))

(defn force-reload! []
  (rf/clear-subscription-cache!)
  (swap! cnt inc))

(figwheel/watch-and-reload
  :websocket-url (str "ws://" env.dev/ip ":3449/figwheel-ws")
  :heads-up-display false
  :jsload-callback force-reload!)

(core/init)
