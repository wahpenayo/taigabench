(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-10-29"
      :date "2017-11-20"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taiga.bench.scripts.classify.ontime.mincount
  
  (:require [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taiga.bench.classify.ontime.traintest :as traintest]))
;; clj src\scripts\clojure\taiga\bench\scripts\ontime\mincount.clj > ontime.mincount.txt
;;----------------------------------------------------------------
(doseq [learner [taiga/positive-fraction-probability
                 taiga/majority-vote-probability]]
  (doseq [suffix [#_"10m" "1m" "0.1m" "0.01m"]]
    (doseq [mincount [2047 511 127 31 7]]
      (traintest/traintest 
        suffix learner (assoc ontime/prototype :mincount mincount)))))
;;----------------------------------------------------------------
    