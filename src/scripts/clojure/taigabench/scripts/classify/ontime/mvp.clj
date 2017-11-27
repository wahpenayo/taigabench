(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2017-11-26"
      :date "2017-11-26"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.classify.ontime.mvp
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.classify.ontime.data :as data]
            [taigabench.classify.ontime.traintest :as traintest]))
;; clj src\scripts\clojure\taigabench\scripts\classify\ontime\mvp.clj > ontime.mvp.txt
;; clj12g src\scripts\clojure\taigabench\scripts\classify\ontime\mvp.clj > ontime.mvp.txt
;; clj48g src\scripts\clojure\taigabench\scripts\classify\ontime\mvp.clj > ontime.mvp.txt
;;----------------------------------------------------------------
(doseq [[mincount suffixes] [[10 ["0.01m"]]]];; "0.1m" "1m" "10m"]]]]
  (with-open [w (z/print-writer 
                  (data/output-file "taiga-mvp.results" "csv"))]
    (.println w 
      "model,ntrain,ntest,datatime,traintime,predicttime,auctime,auc")
    (doseq [suffix suffixes]
      (System/gc)
      (println "taiga" mincount suffix)
      (let [learner  taiga/majority-vote-probability
            results (traintest/traintest 
                      suffix 
                      learner 
                      (assoc traintest/prototype
                             :maxdepth 1024
                             :mincount mincount
                             :nterms 500))
            ^String line (s/join "," ["taiga-mvp"
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
