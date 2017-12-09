(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-08"
      :doc "Public airline ontime data for benchmarking:
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.l2.ontime.traintest
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.pt :as pt]
            [taigabench.metrics :as metrics]
            [taigabench.ontime.data :as data]))
;;----------------------------------------------------------------
(def prototype 
  {:attributes data/l2-attributes
   :csv-reader #(data/read-tsv-file % #"\,")
   :bin-reader data/read-binary-file
   :bin-writer data/write-binary-file
   :mincount 57
   :nterms 127
   :maxdepth 1024})
;;----------------------------------------------------------------
(defn traintest [suffix learner options]
  (let [mincount (:mincount options)
        model-name (str "taiga-" (z/name learner))
        label  (str "taiga-" suffix)
        start (System/nanoTime)
        train (data/read-data-file (str "train-" suffix))
        test (data/read-data-file "test")
        _(System/gc)
        datatime (/ (double (- (System/nanoTime) start)) 
                    1000000000.0)
        options (assoc options :data train)
        start (System/nanoTime)
        ^clojure.lang.IFn$OOD model (learner options)
        traintime (/ (double (- (System/nanoTime) start)) 
                     1000000000.0)
        start (System/nanoTime)
        attributes (:attributes options)
        test (z/pmap #(assoc 
                        % 
                        :prediction (.invokePrim model attributes %)) 
                     test)
        predicttime (/ (double (- (System/nanoTime) start)) 
                       1000000000.0)
        start (System/nanoTime)
        ^clojure.lang.IFn$OD truth (:ground-truth attributes)
        ^clojure.lang.IFn$OD prediction (:prediction attributes)
        rmse (z/rms-difference truth prediction test)
        rmsetime (/ (double (- (System/nanoTime) start)) 
                    1000000000.0)]
    (pt/write-predictions 
      truth prediction test 
      (data/output-file "l2" label "pred.csv.gz"))
  {:model model-name 
   :ntrain (z/count train)
   :ntest (z/count test) 
   :datatime datatime
   :traintime traintime 
   :predicttime predicttime
   :rmsetime rmsetime
   :rmse rmse}))
;;----------------------------------------------------------------