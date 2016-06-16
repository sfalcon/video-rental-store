(ns video-rental-store.server
  (use [video-rental-store.protocols])
  (:require [bidi.ring :refer (make-handler)]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [liberator.core :refer [defresource]]
            [liberator.representation :refer [as-response]]
            [ring.adapter.jetty :refer (run-jetty)]
            [video-rental-store.protocols]
            [video-rental-store.films :as films]
            [video-rental-store.customers :as customers])
  (:import (me.sfalcon.film Status)))

(declare film customer renting)

(defresource
  film
  {:allowed-methods       [:get :post :put :delete]
   :available-media-types ["application/json"]
   :malformed?            (fn [ctx]
                            (let [film (get-in ctx [:request :body])]
                              (films/is-invalid? film)))
   :exists?               (fn exists-handler [ctx]
                            (let [id (get-in ctx [:request :route-params :id])]
                              (when-let [film (lookup (films/film {:id id}))]
                                (assoc ctx :film film))))
   :handle-ok             (fn read-handler [ctx]
                            (-> (:film ctx) films/into-map))
   :post!                 (fn post-handler [ctx]
                            (let [film (-> (get-in ctx [:request :body])
                                           films/film)]
                              (-> (create! film) films/into-map)))
   :put!                  (fn update-handler [ctx]
                            (let [id (-> ctx :film .getId)
                                  film (-> (get-in ctx [:request :body])
                                           films/film (doto (.setId id)))]
                              (-> (update! film) films/into-map)))
   :delete!               (fn delete-handler [ctx]
                            (let [id (get-in ctx [:request :route-params :id])]
                              (delete! (films/film {:id id}))))})

(defresource
  customer
  {:allowed-methods       [:get :post :delete]
   :available-media-types ["application/json"]
   :malformed?            (fn [ctx]
                            (let [customer (get-in ctx [:request :body])]
                              (customers/is-invalid? (customers/customer customer))))
   :exists?               (fn exists-handler [ctx]
                            (let [id (get-in ctx [:request :route-params :id])]
                              (when-let [customer (lookup (customers/customer {:id id}))]
                                (assoc ctx :customer customer))))
   :handle-ok             (fn read-handler [ctx]
                            (-> (:customer ctx) customers/into-map))
   :post!                 (fn post-handler [ctx]
                            (let [customer (-> (get-in ctx [:request :body])
                                               customers/customer)]
                              (-> (create! customer) customers/into-map)))
   :delete!               (fn delete-handler [ctx]
                            (let [id (get-in ctx [:request :route-params :id])]
                              (delete! (customers/customer {:id id}))))})

(defresource
  renting
  {:allowed-methods       [:post]
   :available-media-types ["application/json"]
   ;allow the operation only if all films are rentable
   :allowed?              (fn allowed-handler [ctx]
                            (let [films (->> (or (get-in ctx [:request :body :films])
                                                 [(get-in ctx [:request :route-params :film-id])])
                                             (map #(lookup (films/film {:id %}))))]
                              (when (every? (complement nil?) films)
                                (when (every? #(= (.getRentStatus %) Status/RENTABLE) films)
                                  (assoc ctx :films films)))))
   ;check that both customer and the films exist
   :exists?               (fn exists-handler [ctx]
                            (let [id (get-in ctx [:request :route-params :id])
                                  customer (lookup (customers/customer {:id id}))
                                  films (:films ctx)]
                              (when (and customer
                                         (not-any? nil? films))
                                (-> ctx
                                    (assoc :customer customer)
                                    (assoc :films films)))))
   :handle-created        (fn created-handler [ctx]
                            {:charges (:charges ctx)})
   :post!                 (fn post-handler [{:keys [films customer] :as ctx}]
                            (let [days (Integer. (get-in ctx [:request :route-params :days]))
                                  rent-film-charge #(-> % (.rent days))
                                  charges (loop [charges 0
                                                 films films]
                                            (if (empty? films)
                                              charges
                                              (recur (+ charges (rent-film-charge (first films))) (rest films))))]
                              ;transaction to lock renting and customer assignment
                              (dosync
                                (dorun (map #(. customer rent %) films))
                                (update! customer))
                              (assoc ctx :charges charges)))})

(def routes
  ["/" {"film"                                                  film
        ["film/" :id]                                           film
        "customer"                                              customer
        ["customer/" :id]                                       customer
        ["customer/" :id "/rent/days/" :days]                   renting
        ["customer/" :id "/rent-film/" :film-id "/days/" :days] renting
        ;["customer" :id "return"]                       return
        ;["customer" :id "return/" :film-id]             return
        }])

(def app
  (-> routes
      make-handler
      (wrap-json-body {:keywords? true})
      wrap-json-response))

;Uncomment if you want to debug on the repl
#_(defonce server (run-jetty #'app {:port 3000 :join? false}))
#_(defn restart-server []
  (.stop server)
  (.start server))

(defn -main [& args]
  (run-jetty #'app {:port 3000 :join? false}))