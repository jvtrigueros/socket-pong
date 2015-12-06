(ns socket-pong.handler
  (:use [compojure.core])
  (:require [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as response]))

(defroutes app-routes
           (GET "/" [] (response/resource-response "index.html" {:root "public"}))
           (route/resources "/")
           (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-keyword-params
      middleware/wrap-json-params
      middleware/wrap-json-response))
