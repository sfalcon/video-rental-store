(ns video-rental-store.test.films
  (:require [video-rental-store.protocols :refer [create! update! lookup delete!]])
  (:use [midje.sweet]
        [video-rental-store.films])
  (:import (me.sfalcon.film RegularFilm)))

(defn mockFilm [days-after-deadline]
  (proxy [RegularFilm] []
    (returnIt []
      (-> (* 30 days-after-deadline)))))

(dosync
  (alter id (constantly 0)))
(dosync
  (alter films-store (constantly {})))

(facts "about saving films"
       (fact "can save a new film"
             (-> {:title "The spark" :type "regular"} film create!) => truthy
             (-> {:title "The spark 2" :type "regular"} film create!) => truthy)
       (fact "can update an existing film"
             (-> {:id 2 :title "The spark 2" :type "new"} film update! into-map)
             => {:id 2 :title "The spark 2" :status "RENTABLE" :type "NewFilm"}))

(facts "about reading saved films"
       (fact "can read a saved film"
             (-> {:id 1} film lookup into-map) => {:id 1 :title "The spark" :type "RegularFilm" :status "RENTABLE"}
             (-> {:id 2} film lookup into-map) => {:id 2 :title "The spark 2" :type "NewFilm" :status "RENTABLE"})
       (fact "cannot read a non saved film"
             (-> {:id 8} film lookup) => falsey))

(facts "about deleting films"
       (fact "can delete a film"
             (-> {:id 1} film delete!) => true))

(facts "about renting a film"
       (fact "must rent for at least 1 day"
             (-> {:title "Some film"} film (.rent 0)) => (throws AssertionError)
             (-> {:title "Some film"} film (.rent -2)) => (throws AssertionError)
             )
       (let [matrix11 (film {:title "Matrix 11" :type "new"})
             spiderman5 (film {:title "Spiderman 5" :type "regular"})]
         (fact "renting a new film costs its price times days"
               (. matrix11 rent 1) => 40.0)
         (fact "trying to rent and already rented film throws an error"
               (. matrix11 rent 1) => (throws IllegalStateException)
               (. matrix11 returnIt))
         (fact "renting a regular film costs its basic price; up to its maximum rentable days of 3"
               (. spiderman5 rent 2) => 30.0
               (. spiderman5 returnIt)
               (. spiderman5 rent 3) => 30.0
               (. spiderman5 returnIt))
         (fact "when renting a film by more than its maximum number of rentable days,
         the customer is charged for the renting price and the excess upfront"
               (. matrix11 rent 3) => 120.0
               (. spiderman5 rent 4) => 60.0
               (. matrix11 returnIt)
               (. spiderman5 returnIt))
         ;;TODO - mock return dates if possible instead of mocking price
         (fact "when returned late, it is charged by days times number of days after the date deadline"
               (let [spiderman6 (mockFilm 2)]               ; 2 days after deadline
                 (. spiderman6 returnIt) => 60.0))))