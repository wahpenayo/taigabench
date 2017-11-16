(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2017-01-30"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taiga.bench.scripts.ontime.bench
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taiga.bench.data.ontime :as data]
            [taiga.bench.ontime :as ontime]))
;; clj src\scripts\clojure\taiga\bench\scripts\ontime\bench.clj > ontime.bench.txt
;;------------------------------------------------------------------------------
(doseq [[mincount suffixes] [[10 ["0.01m" "0.1m" "1m" "10m"]]]]
  (with-open [w (z/print-writer (data/output-file "taiga.results" "csv"))]
    (.println w "model,ntrain,ntest,datatime,traintime,predicttime,auctime,auc")
    (doseq [suffix suffixes]
      (System/gc)
      (println "taiga" mincount suffix)
      (let [learner  taiga/positive-fraction-probability
            results (ontime/traintest 
                      suffix 
                      learner 
                      (assoc ontime/prototype
                             :maxdepth 20
                             :mincount mincount
                             :nterms 500))]
        (.println w ^String (s/join "," ["taiga"
                                         (:ntrain results)
                                         (:ntest results)
                                         (:datatime results)
                                         (:traintime results)
                                         (:predicttime results)
                                         (:auctime results)
                                         (:auc results)]))
        (.flush w)))))
;;------------------------------------------------------------------------------
