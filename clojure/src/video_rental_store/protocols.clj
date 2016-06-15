(ns video-rental-store.protocols)

(defprotocol Resource
  "Clojure interface to manage CRUD resources"
  (create! [resource])
  (lookup [resource-id])
  (update! [resource])
  (delete! [resource-id]))
