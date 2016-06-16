(ns video-rental-store.test.customers
  (:require [video-rental-store.protocols :refer [create! update! lookup delete!]]
            [video-rental-store.films :as films])
  (:use [midje.sweet]
        [video-rental-store.customers]))

(dosync
  (alter id (constantly 0)))
(dosync
  (alter customers-store (constantly {})))

(facts "about saving customers"
       (fact "can save a new customer"
             (-> (customer) create!) => truthy
             (-> (customer) create!) => truthy))

(facts "about reading saved customers"
       (fact "can read a saved customer"
             (-> {:id 1} customer lookup into-map) => {:id 1 :bonusPoints 0}
             (-> {:id 2} customer lookup into-map) => {:id 2 :bonusPoints 0})
       (fact "cannot read a non saved customer"
             (-> {:id 8} customer lookup) => falsey))

(facts "about deleting customers"
       (fact "can delete a customer"
             (-> {:id 1} customer delete!) => true))

(facts "about customers renting films"
       (fact "a customer can only be assigned films he has rented"
             (let [rented-film (doto (films/film {:id 1}) (.rent 1))
                   not-rented-film (films/film {:id 2})]
               (-> {:id 1} customer (.rent rented-film)) => nil
               (-> {:id 1} customer (.rent not-rented-film)) => (throws IllegalArgumentException)))
       (fact "a customer cannot return a film he doesn't hold rented"
             (let [customer (-> {:id 1} customer)]
               (.returnFilm customer (films/film {:id 1})) => (throws IllegalStateException))))