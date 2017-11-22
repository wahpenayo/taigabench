(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-11-15"
      :date "2017-11-21"
      :doc "Public airline ontime data for benchmarking:
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.l2.ontime.traintest
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.pt :as pt]
            [taigabench.metrics :as metrics]
            [taigabench.classify.ontime.data :as data]))
;;------------------------------------------------------------------------------
(def prototype 
  {:attributes data/attributes
   :csv-reader #(data/read-tsv-file % #"\,")
   :bin-reader data/read-binary-file
   :bin-writer data/write-binary-file
   ;;:mincount 1
   :nterms 500})
;;------------------------------------------------------------------------------
(defn traintest [suffix learner options]
  (let [mincount (:mincount options)
        model-name (str "taiga-" (z/name learner) "-" mincount)
        label  (str model-name "-" suffix)
        start (System/nanoTime)
        train ((:csv-reader options) 
                (data/data-file (str "train-" suffix) "csv.gz"))
        test ((:csv-reader options) 
               (data/data-file "test" "csv.gz"))
        _(System/gc)
        datatime (/ (double (- (System/nanoTime) start)) 
                    1000000000.0)
        #_(println "test:" (z/count test))
        #_(data/write-binary-file 
            test (data/data-file (str "test-" suffix) "bin.gz"))
        options (assoc options :data train)
        #_(println "train:" (z/count train))
        #_(data/write-binary-file 
           train (data/data-file (str "train-" suffix) "bin.gz"))
        start (System/nanoTime)
        ^clojure.lang.IFn$OOD model (learner options)
        traintime (/ (double (- (System/nanoTime) start)) 
                     1000000000.0)
        ;;model-file (data/output-file label "edn.gz")
        #_(io/make-parents model-file)
        #_(taiga/write-edn model model-file)
        start (System/nanoTime)
        attributes (:attributes options)
        test (z/pmap #(assoc 
                        % 
                        :score (.invokePrim model attributes %)) 
                     test)
        predicttime (/ (double (- (System/nanoTime) start)) 
                       1000000000.0)
        #_train #_(z/seconds
                (print-str "predict train" label) 
                (z/pmap #(assoc 
                           % 
                           :score (.invokePrim model attributes %)) 
                       train))
        start (System/nanoTime)
        ^clojure.lang.IFn$OD truth (:ground-truth attributes)
        ^clojure.lang.IFn$OD score (:prediction attributes)
        auc (metrics/roc-auc truth score test)
        auctime (/ (double (- (System/nanoTime) start)) 
                   1000000000.0)]
    (pt/write-predictions 
      truth score test (data/output-file label "pred.tsv.gz"))
    #_(println "Train AUC:" model-name suffix 
               (metrics/roc-auc truth score train))
    #_(data/write-tsv-file 
      test (data/output-file (str "test-" label) "tsv.gz"))
    #_(data/write-binary-file 
      test (data/output-file (str "test-" label) "bin.gz"))
    #_(data/write-tsv-file 
      train (data/output-file (str "train-" label) "tsv.gz"))
    #_(data/write-binary-file 
      train (data/output-file (str "train-" label) "bin.gz"))
  {:model model-name 
   :ntrain (z/count train)
   :ntest (z/count test) 
   :datatime datatime
   :traintime traintime 
   :predicttime predicttime
   :auctime auctime
   :auc auc}))
;;------------------------------------------------------------------------------
