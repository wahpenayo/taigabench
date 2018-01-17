(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-10"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.l2.ontime.bench
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.ontime.data :as data]
            [taigabench.l2.ontime.traintest :as l2]))
;; clj12g src\scripts\clojure\taigabench\scripts\l2\ontime\bench.clj > ontime.bench.txt
;; clj48g src\scripts\clojure\taigabench\scripts\l2\ontime\bench.clj > ontime.bench.txt
;;----------------------------------------------------------------
(with-open [w (z/print-writer 
                (data/output-file "l2" "taiga.results" "csv"))]
  (.println w 
    "model,ntrain,ntest,datatime,traintime,predicttime,rmsetime,rmse")
  (doseq [suffix ["32768" "131072" "524288" "2097152" "8388608" "33554432"]]
    (System/gc)
    (println "taiga" suffix)
    (let [results (l2/traintest 
                    suffix taiga/mean-regression l2/prototype)
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
