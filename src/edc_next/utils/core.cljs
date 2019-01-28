(ns edc-next.utils.core
  (:require [cljs.spec.alpha :as s]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [oops.core :refer [oget]]
            [superstring.core :as str]
            [taoensso.timbre :as timbre]
            [clojure.walk :refer [postwalk]]))


(defn check-and-throw [spec val]
  (when-not (s/valid? spec val)
    (let [msg (str "spec check failed: " (s/explain-str spec val))]
      (timbre/error msg)
      (throw (ex-info msg {})))))


(defn elem->val [elem]
  (oget elem :target :value))


(defn transform-keys
  ([m f]
   (postwalk (fn [x]
               (if (map? x)
                 (into {} (map (fn [[k v]] [(f k) v]) x))
                 x))
             m))
  ([m]
   (transform-keys m #(keyword (str/lisp-case %)))))


(defn transform-maps [coll]
  (into []
    (map (fn [m] (into {} (map (fn [[k v]] {(keyword (str/lisp-case k)) v}) m))))
    coll))


(defn ->px [v & vs]
  (str/trim
    (reduce
      (fn [acc v] (str acc v "px "))
      (str v "px ")
      vs)))


(def flush-after (rf/after (fn [] (r/flush))))
