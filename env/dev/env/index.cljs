(ns env.index
  (:require [env.dev :as dev]))

;; undo main.js goog preamble hack
(set! js/window.goog js/undefined)

(-> (js/require "figwheel-bridge")
    (.withModules #js {"./assets/icons/loading.png" (js/require "../../../assets/icons/loading.png"), "react-native-router-flux" (js/require "react-native-router-flux"), "firebase/firestore" (js/require "firebase/firestore"), "expo" (js/require "expo"), "./assets/images/cljs.png" (js/require "../../../assets/images/cljs.png"), "./assets/icons/app.png" (js/require "../../../assets/icons/app.png"), "firebase" (js/require "firebase"), "react-native" (js/require "react-native"), "react" (js/require "react"), "./assets/firebase-config.json" (js/require "../../../assets/firebase-config.json"), "react-native-paper" (js/require "react-native-paper"), "create-react-class" (js/require "create-react-class"), "@expo/vector-icons" (js/require "@expo/vector-icons")}
)
    (.start "main" "expo" "192.168.8.62"))
