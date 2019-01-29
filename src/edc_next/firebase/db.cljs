(ns edc-next.firebase.db)

(def firebase-config (js/require "./assets/firebase-config.json"))


(def state {:firebase {:app-info (js->clj firebase-config :keywordize-keys true)}})