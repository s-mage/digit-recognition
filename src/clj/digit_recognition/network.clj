(ns digit-recognition.network
  (:import [org.deeplearning4j.datasets.iterator DataSetIterator]
           [org.deeplearning4j.datasets.iterator.impl MnistDataSetIterator]
           [org.deeplearning4j.eval Evaluation]
           [org.deeplearning4j.nn.api OptimizationAlgorithm]
           [org.deeplearning4j.nn.conf MultiLayerConfiguration NeuralNetConfiguration NeuralNetConfiguration$Builder Updater]
           [org.deeplearning4j.nn.conf.layers DenseLayer$Builder OutputLayer$Builder]
           [org.deeplearning4j.nn.multilayer MultiLayerNetwork]
           [org.deeplearning4j.nn.weights WeightInit]
           [org.deeplearning4j.optimize.listeners ScoreIterationListener]
           [org.nd4j.linalg.factory Nd4j]
           [org.nd4j.linalg.api.ndarray INDArray]
           [org.nd4j.linalg.dataset DataSet]
           [org.nd4j.linalg.lossfunctions LossFunctions LossFunctions$LossFunction]))

(def counts
  {:rows 28
   :cols 28
   :outputs 10
   :batch 128
   :epochs 15
   :seed 123})

(defn load-train []
  (MnistDataSetIterator. (:batch counts) true (:seed counts)))

(defn load-test []
  (MnistDataSetIterator. (:batch counts) false (:seed counts)))

(def data {:train (load-train) :test (load-test)})

(def network-config
  (-> (NeuralNetConfiguration$Builder.)
      (.seed (:seed counts))
      (.optimizationAlgo OptimizationAlgorithm/STOCHASTIC_GRADIENT_DESCENT)
      (.iterations 1)
      (.learningRate 0.006)
      (.updater Updater/NESTEROVS)
      (.momentum 0.9)
      (.regularization true)
      (.l2 1e-4)
      (.list 2)
      (.layer 0 (-> (DenseLayer$Builder.)
                    (.nIn (* (:rows counts) (:cols counts)))
                    (.nOut 1000)
                    (.activation "relu")
                    (.weightInit WeightInit/XAVIER)
                    (.build)))
      (.layer 1 (-> (OutputLayer$Builder. LossFunctions$LossFunction/NEGATIVELOGLIKELIHOOD)
                    (.nIn 1000)
                    (.nOut (:outputs counts))
                    (.activation "softmax")
                    (.weightInit WeightInit/XAVIER)
                    (.build)))
      (.pretrain false)
      (.backprop true)
      (.build)))

(defn build-model []
 (println "building model...")
 (let [m (MultiLayerNetwork. network-config)]
   (.init m)
   (.setListeners m (list (ScoreIterationListener. 1)))
   (println "building done.")
   m))

(defn train-model [m]
  (println "training model...")
  (dotimes [i (:epochs counts)] (.fit m (:train data)))
  (println "training done.")
  m)

(defn test-model [m]
  (println "evaluate model...")
  (let [e (Evaluation. (:outputs counts))]
    (while (.hasNext (:test data))
      (let [d (.next (:test data))]
        (.eval e (.getLabels d) (.output m (.getFeatureMatrix d)))))
    (println "evaluation done.")
    (println (.stats e))))

(defn serialize
  ([m] (serialize m "params.bin" "conf.json"))
  ([m p c]
   (let [params-file (java.io.DataOutputStream. (java.io.FileOutputStream. p))]
     (Nd4j/write (.params m) params-file)
     (spit c (.toJson (.getLayerWiseConfigurations m))))))

(defn deserialize
  ([] (deserialize "params.bin" "conf.json"))
  ([p c]
   (let [params-file (java.io.DataInputStream. (java.io.FileInputStream. p))
         params (Nd4j/read params-file)
         config (MultiLayerConfiguration/fromJson (slurp c))
         result (MultiLayerNetwork. config)]
     (.init result)
     (.setParameters result params)
     result)))

(defn ndarray-argmax [a]
  (let [to-vector (map-indexed vector (map #(.getFloat a %) (range 10)))]
    (first (apply max-key second to-vector))))

(defn recognize [m features]
  (let [to-nn (Nd4j/create (float-array features) (int-array [1 784]))
        nn-output (.output m to-nn)]
    (ndarray-argmax nn-output)))
