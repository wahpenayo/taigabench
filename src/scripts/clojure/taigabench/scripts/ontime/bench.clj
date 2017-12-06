(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-06"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.ontime.bench
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.ontime.data :as data]
            [taigabench.classify.ontime.traintest :as cl]
            [taigabench.l2.ontime.traintest :as l2]
            [taigabench.qcost.ontime.traintest :as qcost]))
;; clj12g src\scripts\clojure\taigabench\scripts\ontime\bench.clj > ontime.bench.txt
;; clj48g src\scripts\clojure\taigabench\scripts\ontime\bench.clj > ontime.bench.txt
;; clj56g src\scripts\clojure\taigabench\scripts\ontime\bench.clj > ontime.bench.txt
;;----------------------------------------------------------------
(def suffixes 
  ["8192" "65536" "524288" "4194304" "33554432"])
;;----------------------------------------------------------------
(with-open [w (z/print-writer 
                (data/output-file "qcost" "taiga.results" "csv"))]
  (.println w 
    "model,ntrain,ntest,datatime,traintime,predicttime,qcosttime,qcost")
  (doseq [suffix suffixes]
    (System/gc)
    (println "taiga" suffix)
    (let [results (qcost/traintest 
                    suffix 
                    taiga/real-probability-measure
                    qcost/prototype)
          ^String line (s/join "," ["taiga"
                                    (:ntrain results)
                                    (:ntest results)
                                    (:datatime results)
                                    (:traintime results)
                                    (:predicttime results)
                                    (:qcosttime results)
                                    (:qcost results)])]
      (.println w line)
      (.flush w))))
;;----------------------------------------------------------------
#_(with-open [w (z/print-writer 
                (data/output-file "l2" "taiga.results" "csv"))]
  (.println w 
    "model,ntrain,ntest,datatime,traintime,predicttime,rmsetime,rmse")
  (doseq [suffix suffixes]
    (System/gc)
    (let [results (z/seconds
                    (print-str "taiga" suffix)
                    (l2/traintest 
                      suffix taiga/mean-regression l2/prototype))
          ^String line (s/join "," ["taiga"
                                    (:ntrain results)
                                    (:ntest results)
                                    (:datatime results)
                                    (:traintime results)
                                    (:predicttime results)
                                    (:rmsetime results)
                                    (:rmse results)])]
      (.println w line)
      (.flush w))))
;;----------------------------------------------------------------
#_(with-open [w (z/print-writer 
               (data/output-file "classify" "taiga.results" "csv"))]
 (.println w 
   "model,ntrain,ntest,datatime,traintime,predicttime,auctime,auc")
 (doseq [suffix suffixes]
   (doseq [learner [taiga/majority-vote-probability
                    taiga/positive-fraction-probability]]
     (System/gc)
     (println "taiga" (cl/model-string learner) suffix)
     (let [results (cl/traintest 
                     suffix learner cl/prototype)
           ^String line (s/join "," [(cl/model-string learner)
                                     (:ntrain results)
                                     (:ntest results)
                                     (:datatime results)
                                     (:traintime results)
                                     (:predicttime results)
                                     (:auctime results)
                                     (:auc results)])]
       (.println w line)
       (.flush w)))))
;;----------------------------------------------------------------
