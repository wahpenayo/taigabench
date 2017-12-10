(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-10
      :doc "Public airline "ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.classify.ontime.bench
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.ontime.data :as data]
            #_[taigabench.classify.ontime.data :as data]
            [taigabench.classify.ontime.traintest :as cl]))
;; clj12g src\scripts\clojure\taigabench\scripts\classify\ontime\bench.clj > ontime.bench.txt
;; clj48g src\scripts\clojure\taigabench\scripts\classify\ontime\bench.clj > ontime.bench.txt
;;----------------------------------------------------------------
(with-open [w (z/print-writer 
                (data/output-file "classify" "taiga.results" "csv"))]
  (.println w 
    "model,ntrain,ntest,datatime,traintime,predicttime,auctime,auc")
  (doseq [suffix ["32768" "131072" "524288" "2097152" "8388608" "33554432"]]
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
