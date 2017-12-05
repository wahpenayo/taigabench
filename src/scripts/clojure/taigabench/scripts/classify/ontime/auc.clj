(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-11-10"
      :date "2017-11-21"
      :doc "Compute auc in one place for all benchmarked libraries, to check that
            AUC imp-lementations are consistent.
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.ontime.classify.auc
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taigabench.pt :as pt]
            [taigabench..metrics :as metrics]
            [taigabench.classify.ontime.data :as data]))
;; clj src\scripts\clojure\taigabench\scripts\classify\ontime\auc.clj
;;------------------------------------------------------------------------------
(with-open [w (z/print-writer (data/output-file "auc" "csv"))]
  (.println w "model,ntrain,ntest,auc")
  (doseq [[prefix suffixes] 
          [["taiga" ["0.01m" "0.1m" "1m" "10m"]]
           ["single.randomForest" ["0.01m" "0.1m"]]
           ["h2o" ["0.01m" "0.1m" "1m" "10m"]]
           ["xgboost" ["0.01m" "0.1m" "1m" "10m"]]]]
    (doseq [suffix suffixes]
      (let [fname (str prefix "-" suffix)
            pt (pt/read-tsv-file (data/output-file fname "pred.csv.gz") #",")
            pt (z/drop-missing pt/truth pt/prediction pt)
            ntrain (int (* 1000000 (Double/parseDouble (s/replace suffix "m" ""))))
            ntest (z/count pt)]
        (println fname ntrain ntest)
        (let [auc (metrics/roc-auc pt/truth pt/prediction pt)]
          (.println w 
            ^String (s/join "," [fname (str ntrain) (str ntest) (str auc)])))))))
;;------------------------------------------------------------------------------
