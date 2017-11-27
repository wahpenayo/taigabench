(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2017-01-30"
      :date "2017-11-26"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.classify.ontime.bench
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.classify.ontime.data :as data]
            [taigabench.classify.ontime.traintest :as traintest]))
;; clj src\scripts\clojure\taigabench\scripts\classify\ontime\bench.clj > ontime.bench.txt
;; clj12g src\scripts\clojure\taigabench\scripts\classify\ontime\bench.clj > ontime.bench.txt
;; clj48g src\scripts\clojure\taigabench\scripts\classify\ontime\bench.clj > ontime.bench.txt
;;----------------------------------------------------------------
(with-open [w (z/print-writer 
                (data/output-file "taiga.results" "csv"))]
  (.println w 
    "model,ntrain,ntest,datatime,traintime,predicttime,auctime,auc")
  (doseq [suffix ["0.01m"]];; "0.1m" "1m" "10m"]]
    (doseq [[learner xxx] 
            [[taiga/majority-vote-probability "mvp"]
             [taiga/positive-fraction-probability "pfp"]]]
      (System/gc)
      (println "taiga" xxx mincount suffix)
      (let [results (traintest/traintest 
                      suffix 
                      learner 
                      (assoc traintest/prototype
                             :maxdepth 1024
                             :mincount mincount
                             :nterms 500))
            ^String line (s/join "," [(str "taiga-" xxx)
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
