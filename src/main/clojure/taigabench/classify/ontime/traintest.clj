(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-04"
      :doc "Public airline ontime data for benchmarking:
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.classify.ontime.traintest
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.pt :as pt]
            [taigabench.metrics :as metrics]
            [taigabench.ontime.data :as data]))
;;----------------------------------------------------------------
(def prototype 
  {:attributes data/classify-attributes
   :csv-reader #(data/read-tsv-file % #"\,")
   :bin-reader data/read-binary-file
   :bin-writer data/write-binary-file
   :mincount 57
   :nterms 127
   :maxdepth 1024})

(defn model-string ^String [learner]
  (str 
    "taiga-"
    (cond (= learner taiga/majority-vote-probability) "mvp"
          (= learner taiga/positive-fraction-probability) "pfp"
          :else (z/name learner))))
;;----------------------------------------------------------------
(defn traintest [suffix learner options]
  (let [mincount (:mincount options)
        model-name (model-string learner)
        label  (str model-name "-" suffix)
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
        test (z/pmap 
               #(assoc 
                  % 
                  :prediction (.invokePrim model attributes %)) 
                     test)
        predicttime (/ (double (- (System/nanoTime) start)) 
                       1000000000.0)
        start (System/nanoTime)
        ^clojure.lang.IFn$OD truth (:ground-truth attributes)
        ^clojure.lang.IFn$OD prediction (:prediction attributes)
        auc (metrics/roc-auc truth prediction test)
        auctime (/ (double (- (System/nanoTime) start)) 
                   1000000000.0)]
    (pt/write-predictions 
      truth prediction test 
      (data/output-file "classify" label "pred.csv.gz"))
    {:model model-name 
     :ntrain (z/count train)
     :ntest (z/count test) 
     :datatime datatime
     :traintime traintime 
     :predicttime predicttime
     :auctime auctime
     :auc auc}))
;;------------------------------------------------------------------------------
