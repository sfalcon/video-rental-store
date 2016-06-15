(ns video-rental-store.films
  (:require [video-rental-store.protocols :refer [Resource]])
  (:import (me.sfalcon.film NewFilm RegularFilm OldFilm Film)))


(defn film "film constructor"
  [{:keys [id title type]}]
  (doto
    (case type
      ("new" "NewFilm") (new NewFilm)
      ("regular" "RegularFilm") (new RegularFilm)
      ("old" "OldFilm") (new OldFilm)
      (new RegularFilm))
    (.setId (str (or id 0)))
    (.setTitle title)))

(def id (ref 0))
;;TODO - in memory "database", can be replaced with a proper DB
(def films-store (ref {}))

(defn -new-id! []
  (dosync
    (alter id inc)))

(defn is-valid? [film-map]
  "determines whether a clojure map is a correct representation of a Film object"
  (try
    (-> (film film-map) boolean)
    (catch Exception _
      false)))

(def is-invalid? (complement is-valid?))

(defn into-map
  "converts a Film instance into a clojure map representation"
  [film-inst]
  (if film-inst
    (-> film-inst
        bean
        (dissoc :class)
        (update :rentStatus str)
        (assoc :type (-> film-inst type .getSimpleName)))
    {}))

(defn -lookup [film]
  (assert (not (nil? film)))
  (let [{:keys [id]} (bean film)]
    (get @films-store id)))

(extend-type Film
  Resource
  (create! [film-inst]
    (dosync
      (let [id (-new-id!)]
        (. film-inst setId (str id))
        (alter films-store #(assoc % id film-inst))
        film-inst)))
  (lookup [film-inst]
    (-lookup film-inst))
  (update! [film-inst]
    (let [old-film-map (into-map (-lookup film-inst))
          new-film-map (-> film-inst into-map (dissoc :id))
          updated-film (-> (merge old-film-map new-film-map) film)]
      (dosync
        (alter films-store #(assoc % (:id old-film-map) updated-film)))
      updated-film))
  (delete! [film-instance]
    (-> (dosync
          (alter films-store #(dissoc % (.getId film-instance))))
        boolean)))
