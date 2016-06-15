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
       (fact "a customer can rent films and be charged for them"
             (-> {:id 1} customer (.rent (films/film {:id 1}) 1)) => 30.0)
       (fact "a customer can return a film they've rented and be charged if returning late"
             ;TODO - add tests for late returns when a proper mock is set
             (let [customer (-> {:id 1} customer)
                   film (films/film {:id 1})]
               (-> customer (.rent film 1)) => 30.0
               (-> customer (.returnFilm film)) => 0.0))
       (fact "a customer cannot rent a film that is already rented"
             (let [rented-film (doto (films/film {:id 1})
                                 (.rent 1))]
               (-> {:id 1} customer (.rent rented-film 1))) => (throws IllegalStateException))
       (fact "a customer cannot return a film they don't have rented"
             (let [not-rented-film (films/film {:id 1})]
               (-> {:id 1} customer (.returnFilm not-rented-film))) => (throws IllegalStateException)))