(ns edc-next.firebase.core
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.ratom :as ratom]
            [taoensso.encore :as e]
            [taoensso.timbre :as timbre]
            [oops.core :refer [oget ocall oapply]]))


(def firebase (js/require "firebase"))
;(js-keys firebase)
(def _FIRESTORE (js/require "firebase/firestore"))


(defn delete-field []
  (ocall firebase "firestore.FieldValue.delete"))


(defn promise-wrapper [promise on-success on-failure]
  (when on-success
    (ocall promise "then" #(rf/dispatch on-success)))
  (if on-failure
    (ocall promise "catch" #(rf/dispatch on-failure))
    (ocall promise "catch" #(timbre/error %))))


(defn firestore []
  (ocall firebase "firestore"))


(defn initialize-app [firebase-app-info]
  (ocall firebase "initializeApp" (clj->js firebase-app-info)))


;(defn- query [ref where order-by limit
;              start-at start-after end-at end-before]
;  (as-> ref $
;        (if where
;          (reduce
;            (fn [$$ [field-path op value]] (ocall $$ "where" (clj->FieldPath field-path) (clj->js op) (clj->js value)))
;            $ where)
;          $)
;        (if order-by
;          (reduce
;            (fn [$$ order] (ocall $$ "orderBy" (clj->js (nth order 0)) (clj->js (nth order 1 :asc))))
;            $ order-by)
;          $)
;        (if limit (ocall $ "limit" limit) $)
;        (if start-at (.apply (oget $ "startAt") $ (clj->js start-at)) $)
;        (if start-after (.apply (oget $ "startAfter") $ (clj->js start-after)) $)
;        (if end-at (.apply (oget $ "endAt") $ (clj->js end-at)) $)
;        (if end-before (.apply (oget $ "endBefore") $ (clj->js end-before)) $)))


(defmulti path->field (fn [path] (type path)))

(defmethod path->field cljs.core/PersistentVector
  [path]
  (str/join "/" (map name path)))

(defmethod path->field cljs.core/Keyword
  [path]
  (name path))

(defmethod path->field js/String
  [path]
  path)



(defn doc->clj [doc]
  (js->clj (ocall doc "data")
           :keywordize-keys true))


(defn collection->clj [coll]
  (map doc->clj (oget coll "docs")))


(defn query->clj [coll]
  (collection->clj coll))


(defn on-doc-snapshot!
  ([path]
   (on-doc-snapshot! path [:_cache (e/uuid-str)]))
  ([path dispatch & {:keys [on-success on-failure]}]
   (-> (firestore)
       (ocall "doc" (path->field path))
       (ocall "onSnapshot" #(rf/dispatch (conj dispatch (doc->clj %))))
       (cond-> on-success
               (ocall "then" #(rf/dispatch on-success)))
       (ocall "catch" (if on-failure
                        #(rf/dispatch on-failure)
                        #(timbre/error %))))))


(defn on-coll-snapshot!
  ([path]
   (on-coll-snapshot! path [:write-to [:_cache (e/uuid-str)]]))
  ([path dispatch]
   (-> (firestore)
       (ocall "collection" (path->field path))
       (ocall "onSnapshot" (fn [snap]
                             (rf/dispatch (conj dispatch (collection->clj snap))))))))


(defn unsubscribe [path]
  (-> (firestore)
      (ocall "doc" (path->field path))
      (ocall "onSnapshot" (fn []))))


(defn unsubscribe-coll [path]
  (-> (firestore)
      (ocall "collection" (path->field path))
      (ocall "onSnapshot" (fn []))))



(defn get-coll [path & {:keys [on-success on-failure]}]
  (-> (firestore)
      (ocall "collection" (path->field path))
      (ocall "get")
      (cond-> on-success
              (ocall "then" (fn [coll] (rf/dispatch (conj on-success (mapcat #(doc->clj %) coll))))))
      (ocall "catch" (if on-failure
                       #(rf/dispatch on-failure)
                       #(timbre/error %)))))


(defn get-doc [path & {:keys [on-success on-failure]}]
  (-> (firestore)
      (ocall "doc" (path->field path))
      (ocall "get")
      (cond-> on-success
              (ocall "then" #(rf/dispatch (conj on-success (doc->clj %)))))
      (ocall "catch" (if on-failure
                       #(rf/dispatch on-failure)
                       #(timbre/error %)))))


(defn set-doc [path doc & {:keys [on-success on-failure]}]
  (-> (firestore)
      (ocall "doc" (path->field path))
      (ocall "set" (clj->js doc))
      (cond-> on-success (ocall "then" #(rf/dispatch (conj on-success %))))
      (ocall "catch" (if on-failure #(rf/dispatch on-failure) (fn [])))))


(defn update-doc [path doc & {:keys [on-success on-failure]}]
  (-> (firestore)
      (ocall "doc" (path->field path))
      (ocall "update" (clj->js doc))
      (cond-> on-success (ocall "then" #(rf/dispatch (conj on-success %))))
      (ocall "catch" (if on-failure #(rf/dispatch on-failure) (fn [])))))


(defn add-doc [collection doc & {:keys [on-success on-failure]}]
  (-> (firestore)
      (ocall "collection" (path->field collection))
      (ocall "add" (clj->js doc))
      (cond-> on-success (ocall "then" #(rf/dispatch (conj on-success %))))
      (ocall "catch" (if on-failure #(rf/dispatch on-failure) (fn [])))))


(defn del-doc [path & {:keys [on-success on-failure]}]
  (-> (firestore)
      (ocall "doc" (path->field path))
      (ocall "delete")
      (cond-> on-success (ocall "then" #(rf/dispatch (conj on-success %))))
      (ocall "catch" (if on-failure #(rf/dispatch on-failure) (fn [])))))


;(defn query [{:keys [collection where order-by limit
;                     start-at start-after end-at end-before]}]
;  (let [ref (-> (firestore)
;                (ocall "collection" (path->field collection)))]
;    ))


(defn query [{:keys [collection where order-by limit
                     start-at start-after end-at end-before
                     on-success on-failure]}]
  (let [ref (-> (firestore)
                (ocall "collection" (path->field [collection])))]
    (-> (as-> ref $
              (if where
                (reduce
                  (fn [$$ [path op value]] (ocall $$ "where" (path->field path) (clj->js op) (clj->js value)))
                  $ where)
                $)
              (if order-by
                (reduce
                  (fn [$$ order] (ocall $$ "orderBy" (clj->js (nth order 0)) (clj->js (nth order 1 :asc))))
                  $ order-by)
                $)
              (if limit (ocall $ "limit" limit) $)
              (if start-at (oapply $ "startAt" (clj->js start-at)) $)
              (if start-after (oapply $ "startAfter" (clj->js start-after)) $)
              (if end-at (oapply $ "endAt" (clj->js end-at)) $)
              (if end-before (oapply $ "endBefore" (clj->js end-before)) $))
        (ocall "get")
        (cond-> on-success (ocall "then" #(rf/dispatch (conj on-success %))))
        (ocall "catch" (if on-failure #(rf/dispatch on-failure) (fn []))))))