(ns video-rental-store.customers
  (:require [video-rental-store.protocols :refer [Resource]])
  (:import (me.sfalcon.customer Customer)))

;;TODO - very similar to the films namespace,
;;a clear pattern that we could implement with a clojure macro
;;or a more sophisticated data structure

(defn customer "customer constructor"
  ([]
    (new Customer))
  ([{:keys [id]}]
   (doto
     (new Customer)
     (.setId (str (or id 0))))))

(def id (ref 0))
;;TODO - in memory "database", can be replaced with a proper DB
(def customers-store
   (ref {}))                            

(defn -new-id! []
  (dosync
    (alter id inc)))

(defn is-valid? [customer-map]
  "determines whether a clojure map is a correct representation of a customer object"
  (try
    (-> (customer customer-map) boolean)
    (catch Exception _
      false)))

(def is-invalid? (complement is-valid?))

(defn into-map
  "converts a Customer instance into a clojure map representation"
  [customer-inst]
  (if customer-inst
    (-> customer-inst
        bean
        (dissoc :class))
    {}))

(defn -lookup [customer]
  (assert (not (nil? customer)))
  (let [{:keys [id]} (bean customer)]
    (get @customers-store id)))

(extend-type Customer
  Resource
  (create! [customer-inst]
    (dosync
      (let [id (-new-id!)]
        (. customer-inst setId (str id))
        (alter customers-store #(assoc % id customer-inst)))))
  (lookup [customer-inst]
    (-lookup customer-inst))
  (update! [customer-inst]
    ;;since there are no attributes to update for a customer other than its id, we just leave this as placeholder
    customer-inst)
  (delete! [customer-instance]
    (-> (dosync
          (alter customers-store #(dissoc % (.getId customer-instance))))
        boolean)))