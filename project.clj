(defproject digit_recognition "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :source-paths ["src/clj"]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.deeplearning4j/deeplearning4j-core "0.4-rc3.8"]
                 [org.nd4j/nd4j-x86 "0.4-rc3.8"]
                 [org.nd4j/canova-api "0.0.0.14"]
                 [ring "1.4.0"]
                 [http-kit "2.1.18"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [amalloy/ring-gzip-middleware "0.1.3"]
                 [ring/ring-mock "0.3.0"]
                 [route-map "0.0.2"]
                 [prismatic/dommy "1.1.0"]
                 [environ "1.0.1"]
                 [com.taoensso/timbre "4.2.0"]]
  :main ^:skip-aot digit-recognition.core
  :repl-options {:init-ns digit-recognition.server
                 :init (use 'digit-recognition.server :reload)})
