(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-11-03"
      :date "2017-11-17"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taiga.bench.scripts.ontime.classify.distinct
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taiga.bench.classify.ontime.data :as data]))
;; clj src\scripts\clojure\taiga\bench\scripts\classify\ontime\distinct.clj > output\classify\ontime\distinct.txt
;;----------------------------------------------------------------
(let [data (z/mapcat
               #(data/read-tsv-file (data/data-file % "csv") 
                                    #"\,")
               ["test" "valid" 
                "train-0.01m" "train-0.1m" 
                "train-1m" "train-10m"])]
    (println (z/count data))
    (println (z/distinct data/month data))
    (println (sort (z/distinct data/dayofmonth data)))
    (println (z/distinct data/dayofweek data))
    (println (sort (z/distinct data/uniquecarrier data)))
    (println (count (z/distinct data/uniquecarrier data)))
    (println (sort (z/distinct data/origin data)))
    (println (count (z/distinct data/origin data)))
    (println (sort (z/distinct data/dest data)))
    (println (count (z/distinct data/dest data)))
    (println (sort (z/union (z/distinct data/origin data)
                            (z/distinct data/dest data)))))
;;----------------------------------------------------------------
