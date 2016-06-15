(ns video-rental-store.server
  (use [video-rental-store.protocols])
  (:require [bidi.ring :refer (make-handler)]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [liberator.core :refer [defresource]]
            [liberator.representation :refer [as-response]]
            [ring.adapter.jetty :refer (run-jetty)]
            [video-rental-store.protocols]
            [video-rental-store.films :as films]
            [video-rental-store.customers :as customers]))

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



(def routes
  ["/" {"film"           film
        ["film/" :id]    film
        "customer"       customer
        ["customer" :id] customer
        ;["customer" :id "rent"]          renting
        ;["customer" :id "rent" :film-id] renting
        }])

(def app
  (-> routes
      make-handler
      (wrap-json-body {:keywords? true})
      wrap-json-response))

;; TODO - Use components
(defonce server (run-jetty #'app {:port 3000 :join? false}))
(defn restart-server []
  (.stop server)
  (.start server))

