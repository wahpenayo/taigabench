(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-01"
      :doc "Public airline ontime data for benchmarking:
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.qcost.ontime.traintest
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.deciles :as deciles]
            [taigabench.metrics :as metrics]
            [taigabench.ontime.data :as data]))
;;----------------------------------------------------------------
(def prototype 
  {:attributes data/qcost-attributes
   :csv-reader #(data/read-tsv-file % #"\,")
   :bin-reader data/read-binary-file
   :bin-writer data/write-binary-file
   :mincount 57
   :nterms 127
   :maxdepth 1024})
;;----------------------------------------------------------------
(defn traintest [suffix learner options]
  (let [mincount (:mincount options)
        model-name (str "taiga-" (z/name learner) "-" mincount)
        label  (str model-name "-" suffix)
        start (System/nanoTime)
        train (data/read-data-file (str "train-" suffix))
        test (data/read-data-file "test")
        test (z/take 1024 test)
        _(System/gc)
        datatime (/ (double (- (System/nanoTime) start)) 
                    1000000000.0)
        options (assoc options :data train)
        start (System/nanoTime)
        ^clojure.lang.IFn model (learner options)
        traintime (/ (double (- (System/nanoTime) start)) 
                     1000000000.0)
        start (System/nanoTime)
        attributes (:attributes options)
        ^clojure.lang.IFn$OD truth (:ground-truth attributes)
        test (z/pmap 
               #(assoc 
                  % :predictedDistribution (model attributes %))
               test)
        quantile (fn quantile ^double [datum ^double p]
                   (z/quantile 
                     (data/predictedDistribution datum) p))
        deciles (z/pmap 
                  #(deciles/make (.invokePrim truth %) quantile %) 
                  test)
        predicttime (/ (double (- (System/nanoTime) start)) 
                       1000000000.0)
        start (System/nanoTime)
        qcost (z/mean deciles/cost deciles)
        qcosttime (/ (double (- (System/nanoTime) start)) 
                     1000000000.0)
        prfile (data/output-file "qcost" label "pred.tsv.gz")]
    
    (deciles/write-csv
      deciles )
    {:model model-name 
     :ntrain (z/count train)
     :ntest (z/count test) 
     :datatime datatime
     :traintime traintime 
     :predicttime predicttime
     :qcosttime qcosttime
     :qcost qcost
     :predictfile (.getPath prfile)}))
;;----------------------------------------------------------------