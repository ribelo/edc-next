(ns edc-next.transit
  (:require [cognitect.transit :as t]
            [cljs-time.coerce :as dtc])
  (:import [goog.date DateTime UtcDateTime]))


(def readers
  {:handlers
   {"m" (t/read-handler (fn [s] (dtc/from-long (js/parseInt s))))}})


(def writers
  {:handlers
   {UtcDateTime (t/write-handler
                  (constantly "m")
                  (fn [v] (.getTime v))
                  (fn [v] (str (.getTime v))))
    js/Date     (t/write-handler
                  (constantly "m")
                  (fn [v] (.getTime v))
                  (fn [v] (str (.getTime v))))}})


(defn ->json [data]
  (let [w (t/writer :json writers)]
    (t/write w data)))


(defn <-json [data]
  (let [w (t/reader :json readers)]
    (t/read w data)))


(defn ->raw-json [data]
  (let [w (t/writer :json)]
    (t/write w data)))