(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-03"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taiga.bench.scripts.ontime.distinct
  
  (:require [clojure.string :as s]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taiga.bench.data.ontime :as ontime]))
;; clj src\scripts\clojure\taiga\bench\scripts\ontime\distinct.clj > ontime.distinct.txt
;;------------------------------------------------------------------------------
(let [data (z/mapcat
               #(ontime/read-tsv-file (ontime/data-file % "csv") #"\,")
               ["test" "valid" "train-0.01m" "train-0.1m" "train-1m" "train-10m"])]
    (println (z/count data))
    (println (z/distinct ontime/month data))
    (println (sort (z/distinct ontime/dayofmonth data)))
    (println (z/distinct ontime/dayofweek data))
    (println (sort (z/distinct ontime/uniquecarrier data)))
    (println (count (z/distinct ontime/uniquecarrier data)))
    (println (sort (z/distinct ontime/origin data)))
    (println (count (z/distinct ontime/origin data)))
    (println (sort (z/distinct ontime/dest data)))
    (println (count (z/distinct ontime/dest data)))
    (println (sort (z/union (z/distinct ontime/origin data)
                            (z/distinct ontime/dest data)))))
;;------------------------------------------------------------------------------
