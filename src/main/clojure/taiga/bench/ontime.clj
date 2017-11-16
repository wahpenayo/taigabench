(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-15"
      :doc "Public airline ontime data for benchmarking:
            http://stat-computing.org/dataexpo/2009/" }
    
    taiga.bench.ontime
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taiga.bench.pt :as pt]
            [taiga.bench.metrics :as metrics]
            [taiga.bench.data.ontime :as ontime]))
;;------------------------------------------------------------------------------
(def prototype 
  {:attributes ontime/attributes
   :csv-reader #(ontime/read-tsv-file % #"\,")
   :bin-reader ontime/read-binary-file
   :bin-writer ontime/write-binary-file
   ;;:mincount 1
   :nterms 500})
;;------------------------------------------------------------------------------
(defn traintest [suffix learner options]
  (let [mincount (:mincount options)
        model-name (str "taiga-" (z/name learner) "-" mincount)
        label  (str model-name "-" suffix)
        start (System/nanoTime)
        train ((:csv-reader options) 
                (ontime/data-file (str "train-" suffix) "csv"))
        test ((:csv-reader options) (ontime/data-file "test" "csv"))
        _(System/gc)
        datatime (/ (double (- (System/nanoTime) start)) 1000000000.0)
;        _ (println "test:" (z/count test))
;        _ (ontime/write-binary-file 
;            test (ontime/data-file (str "test-" suffix) "bin.gz"))
        options (assoc options :data train)
;        _ (println "train:" (z/count train))
;        _ (ontime/write-binary-file 
;            train (ontime/data-file (str "train-" suffix) "bin.gz"))
        start (System/nanoTime)
        ^clojure.lang.IFn$OOD model (learner options)
        traintime (/ (double (- (System/nanoTime) start)) 1000000000.0)
;        model-file (ontime/output-file label "edn.gz")
;        _ (io/make-parents model-file)
;        _ (taiga/write-edn model model-file)
        start (System/nanoTime)
        attributes (:attributes options)
        test (z/pmap #(assoc % :score (.invokePrim model attributes %)) test)
        predicttime (/ (double (- (System/nanoTime) start)) 1000000000.0)
        #_train #_(z/seconds
                (print-str "predict train" label) 
                (z/pmap #(assoc % :score (.invokePrim model attributes %)) 
                       train))
        start (System/nanoTime)
        ^clojure.lang.IFn$OD truth (:ground-truth attributes)
        ^clojure.lang.IFn$OD score (:prediction attributes)
        auc (metrics/roc-auc truth score test)
        auctime (/ (double (- (System/nanoTime) start)) 1000000000.0)]
    (pt/write-predictions 
      truth score test (ontime/output-file label "pred.tsv.gz"))
    #_(println "Train AUC:" model-name suffix (metrics/roc-auc truth score train))
    #_(ontime/write-tsv-file 
      test (ontime/output-file (str "test-" label) "tsv.gz"))
    #_(ontime/write-binary-file 
      test (ontime/output-file (str "test-" label) "bin.gz"))
    #_(ontime/write-tsv-file 
      train (ontime/output-file (str "train-" label) "tsv.gz"))
    #_(ontime/write-binary-file 
      train (ontime/output-file (str "train-" label) "bin.gz"))
  {:model model-name 
   :ntrain (z/count train)
   :ntest (z/count test) 
   :datatime datatime
   :traintime traintime 
   :predicttime predicttime
   :auctime auctime
   :auc auc}))
;;------------------------------------------------------------------------------
