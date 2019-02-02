(ns edc-next.theme.db
  (:require [edc-next.rnp.core :as rnp]))


(def app-theme (rnp/theme {:colors {:primary       (rnp/color :green500)
                                    :primary-dark  "#087f23"
                                    :primary-light "#80e27e"
                                    :accent        (rnp/color :deepOrange300)
                                    :accent-dark   "#c75b39"
                                    :accent-light  "#ffbb93"}}))

app-theme
(def state {:theme (js->clj app-theme :keywordize-keys true)})
