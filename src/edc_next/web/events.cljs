(ns edc-next.web.events
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.encore :as e]
            [com.rpl.specter :as sp]
            [edc-next.rn.core :as rn]
            [edc-next.rnp.core :as rnp]))


(rf/reg-event-fx
  :frisco/search!
  (fn [_ [_ q]]
    {:expo/open-browser (str "https://www.frisco.pl/q," q "/stn,searchResults")}))


(rf/reg-event-fx
  :dodomku/search!
  (fn [_ [_ q]]
    {:expo/open-browser (str "https://dodomku.pl/szukaj/" q "/0.html")}))


(rf/reg-event-fx
  :google/search!
  (fn [_ [_ q]]
    {:expo/open-browser (str "https://www.google.pl/search?q=" q)}))
