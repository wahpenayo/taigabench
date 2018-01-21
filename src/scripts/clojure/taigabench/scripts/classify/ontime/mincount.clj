(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-01-20"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.classify.ontime.mincount
  
  (:require [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.classify.ontime.traintest :as traintest]))
;; clj src\scripts\clojure\taigabench\scripts\ontime\mincount.clj > ontime.mincount.txt
;;----------------------------------------------------------------
(doseq [learner [taiga/positive-fraction-probability
                 taiga/majority-vote-probability]]
  (doseq [suffix [#_"10m" "1m" "0.1m" "0.01m"]]
    (doseq [mincount [2047 1023 511 255 127 63 31 51 7]]
      (traintest/traintest 
        suffix learner (assoc ontime/prototype :mincount mincount)))))
;;----------------------------------------------------------------
    