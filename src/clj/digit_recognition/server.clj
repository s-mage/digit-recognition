(ns digit-recognition.server
  (:require [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [route-map.core :as rm]
            [org.httpkit.server :refer [run-server]]
            [digit-recognition.router :refer [router]]
            [digit-recognition.network :as nn])
  (:gen-class))

(def m (nn/deserialize))

(defn index [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (slurp (io/resource "index.html"))})

(defn recognize [req]
  (let [features (vec (map read-string (get (:params req) "features[]")))]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (str (nn/recognize m features))}))

(def routes 
  { :GET index
    "recognize" { :POST recognize}})

(def app (router routes))

(defonce server (atom nil))
(defn  stop-server! [] 
  (when-not (nil? server)
    (@server :timeout 100)
    (reset! server nil)))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port 10555))]
    (log/info (format "Starting web server on port %d ..." port))
    (reset! server (run-server #'app {:port port}))))

(def run run-web-server)
(def stop stop-server!)

(defn -main [& [port]]
  (run port))
