(ns digit-recognition.router
  (:require
    [ring.middleware.params         :refer [wrap-params]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.resource       :refer [wrap-resource]]
    [ring.middleware.content-type   :refer [wrap-content-type]]
    [taoensso.timbre :as log]
    [route-map.core :as rm]))

(defn dispatcher [routes {meth :request-method uri :uri :as req}]
  (if-let [res (rm/match [meth uri] routes)]
    ((:match res)  req)
    {:status 302 :body "" :headers {"Location" "/"}}))


(defn wrap-logger [handler]
  (fn [req]
    (log/info req)
    (let [response (handler req)] (log/info response) response)))

(defn router [routes]
  (->
    #(dispatcher routes %)
    wrap-keyword-params
    wrap-params
    (wrap-resource "public")
    wrap-logger))
