(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-01"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.ontime.bench
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.ontime.data :as data]
            [taigabench.classify.ontime.traintest :as cl]
            [taigabench.l2.ontime.traintest :as l2]))
;; clj12g src\scripts\clojure\taigabench\scripts\ontime\bench.clj > ontime.bench.txt
;; clj56g src\scripts\clojure\taigabench\scripts\ontime\bench.clj > ontime.bench.txt
;;----------------------------------------------------------------
(with-open [w (z/print-writer 
                (data/output-file "l2" "taiga.results" "csv"))]
  (.println w 
    "model,ntrain,ntest,datatime,traintime,predicttime,rmsetime,rmse")
  (doseq [suffix ["8192" "65536" "524288" "4194304" "33554432"]]
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
(with-open [w (z/print-writer 
                (data/output-file "classify" "taiga.results" "csv"))]
  (.println w 
    "model,ntrain,ntest,datatime,traintime,predicttime,auctime,auc")
  (doseq [suffix ["8192" "65536" "524288" "4194304" "33554432"]]
    (doseq [[learner xxx] 
            [[taiga/majority-vote-probability "mvp"]
             [taiga/positive-fraction-probability "pfp"]]]
      (System/gc)
      (let [results (z/seconds
                      (print-str "taiga" xxx suffix)
                      (cl/traintest 
                        suffix learner cl/prototype))
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
