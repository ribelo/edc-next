(ns edc-next.rn.core
  (:require [reagent.core :as r]
            [oops.core :refer [oget oget+ ocall ocall+]]))


(def rn (js/require "react-native"))


(defn get-class [class]
  (r/adapt-react-class (oget+ rn class)))


(def app-registry (oget+ rn :AppRegistry))


(defn alert
  ([title]
   (ocall+ (oget+ rn :Alert) :alert title))
  ([title msg]
   (ocall+ (oget+ rn :Alert) :alert title msg)))


(def async-storage (oget+ rn :AsyncStorage))


(defn async-storage-set-item [k v]
  (ocall+ async-storage :setItem k v))


(defn async-storage-get-item [k]
  (ocall+ async-storage :getItem k))


(defn yellow-box
  ([k]
   (ocall+ (oget+ rn :YellowBox) k))
  ([k params]
   (ocall+ (oget+ rn :YellowBox) k (clj->js params))))


(def activity-indicator (get-class :ActivityIndicator))
(def back-handler (oget+ rn :BackHandler))
(defn on-back-press [f] (ocall+ back-handler :addEventListener "hardwareBackPress" f))
(defn remove-back-handler
  ([] (ocall+ back-handler :removeEventListener "hardwareBackPress" (fn [])))
  ([handler] (ocall+ handler :remove)))
(def view (get-class :View))
(def text (get-class :Text))
(def image (get-class :Image))
(def text-input (get-class :TextInput))
(def scroll-view (get-class :ScrollView))
(def button (get-class :Button))
(def touchable-highlight (get-class :TouchableHighlight))
(def picker (get-class :Picker))
(def slider (get-class :Slider))
(def switch (get-class :Switch))
(def status-bar (get-class :StatusBar))
(def flat-list (get-class :FlatList))
(def section-list (get-class :SectionList))
(def virtualized-list (get-class :VirtualizedList))
(def date-picker (oget+ rn :DatePickerAndroid))

(def vibration (oget+ rn :Vibration))
(defn vibrate [pattern] (ocall+ vibration :vibrate pattern))



(def animated (oget+ rn :Animated))

(def animated-view (r/adapt-react-class (oget+ animated :View)))


(defn animated-value [val]
  (let [value (oget+ rn :Animated :Value)]
    (value. val)))


(defn value-interpolate [value {:keys [input-range output-range]}]
  (ocall+ value :interpolate
          (clj->js {:inputRange  input-range
                    :outputRange output-range})))


(defn animated-loop
  ([anim]
   (let [loop (oget+ rn :Animated :loop)]
     (loop. anim)))
  ([anim props]
   (let [loop (oget+ rn :Animated :loop)]
     (loop anim (clj->js props)))))


(defn animated-timing [value {:keys [to-value] :as props}]
  (let [timing (oget+ rn :Animated :timing)]
    (timing value (clj->js (assoc props :toValue to-value)))))


(defn animated-spring [value props]
  (let [spring (oget+ rn :Animated :spring)]
    (spring value (clj->js props))))


(def easing-linear (oget+ rn :Easing :linear))
(def easing-ease (oget+ rn :Easing :ease))
(def easing-ease-out (oget+ rn :Easing :out))

(defn animation-start
  ([anim]
   (ocall anim :start))
  ([anim cb]
   (ocall anim :start cb)))

(def keyboard (oget+ rn :Keyboard))

(def dimensions (oget+ rn :Dimensions))