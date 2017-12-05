(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-01"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.classify.ontime.pfp
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.classify.ontime.data :as data]
            [taigabench.classify.ontime.traintest :as traintest]))
;; clj12g src\scripts\clojure\taigabench\scripts\classify\ontime\pfp.clj > ontime.pfp.txt
;; clj48g src\scripts\clojure\taigabench\scripts\classify\ontime\pfp.clj > ontime.pfp.txt
;;----------------------------------------------------------------
(with-open [w (z/print-writer 
                (data/output-file "taiga-pfp.results" "csv"))]
  (.println w 
    "model,ntrain,ntest,datatime,traintime,predicttime,auctime,auc")
  (doseq [suffix ["8192" "65536" "524288" "4194304" "33554432"]]
    (System/gc)
    (println "taiga" mincount suffix)
    (let [learner  taiga/positive-fraction-probability
          results (traintest/traintest 
                    suffix learner traintest/prototype)
          ^String line (s/join "," ["taiga-pfp"
                                    (:ntrain results)
                                    (:ntest results)
                                    (:datatime results)
                                    (:traintime results)
                                    (:predicttime results)
                                    (:auctime results)
                                    (:auc results)])]
      (.println w line)
      (.flush w))))
;;----------------------------------------------------------------
