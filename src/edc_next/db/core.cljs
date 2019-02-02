(ns edc-next.db.core
  (:require [cljs.spec.alpha :as s]
            [clojure.walk :refer [prewalk postwalk]]
            [re-frame.core :as rf]
            [oops.core :refer [ocall oget]]
            [taoensso.encore :as e]
            [superstring.core :as str]
            [edc-next.rn.core :as rn]
            [edc-next.transit :as t]))


(defn filter-db-keys [db]
  (prewalk
    (fn [node]
      (if (map? node)
        (reduce (fn [acc [k v]]
                  (if-not (str/starts-with? (str k) ":_")
                    (assoc acc k v)
                    acc)) {} node)
        node))
    db))


(let [write-timeout-handler (atom nil)]
  (defn save-async-storage [db & {ms  :ms
                                  :or {ms 250}}]
    (when @write-timeout-handler
      (e/tf-cancel! @write-timeout-handler))
    (reset! write-timeout-handler
            (e/after-timeout ms (rn/async-storage-set-item "edc-next" (-> db (filter-db-keys) (t/->json)))))))


(def ->async-storage (rf/after save-async-storage))


(defn load-async-storage []
  (rn/async-storage-get-item "edc-next"))


(defn clear-async-storage []
  (rn/async-storage-set-item "edc-next" (t/->json {})))


(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))


(def default-db
  {})
