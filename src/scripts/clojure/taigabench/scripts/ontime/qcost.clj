(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-23"
      :doc "Compute decile costs for:<br>
            Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.ontime.qcost
  
  (:require [clojure.string :as s]
            [clojure.pprint :as pp]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.deciles :as deciles]
            [taigabench.ontime.data :as data])
  (:import [java.util Map]))
;; clj12g src\scripts\clojure\taigabench\scripts\ontime\qcost.clj
;;----------------------------------------------------------------
;; read a results file
;;----------------------------------------------------------------
(defn- split [^String s] (s/split s #"\,"))
(defn- head [^String line]
  (mapv #(keyword (s/lower-case (s/replace % "\"" ""))) 
                  (split line)))
(defn- read-results ^Iterable [lib]
  (println "reading" lib "results")
  (let [file (data/output-file 
               "qcost" (str lib ".results") "csv")]
    (with-open [r (z/reader file)]
      (let [lines (line-seq r)
            header (head (first lines))]
        (prn header)
        (z/map #(zipmap header (split %))
               (rest lines))))))
;;----------------------------------------------------------------
;; write all the results with decilecost addded
;;----------------------------------------------------------------
(defn- write-results [^Iterable results]
  (let [file (data/output-file "qcost" "results" "csv")]
    (with-open [w (z/print-writer file)]
      (.println w 
        "model,ntrain,ntest,datatime,traintime,predicttime,decilecost")
      (doseq [result results]
        (.println w 
          ^String (s/join "," [(:model result)
                               (:ntrain result)
                               (:ntest result)
                               (:datatime result)
                               (:traintime result)
                               (:predicttime result)
                               (:decilecost result)]))))))
;;----------------------------------------------------------------
(defn- decile-cost ^Map [^Map record]
  (pp/pprint record)
  (let [model (:model record)
        suffix (:ntrain record)
        label  (str model "-" suffix)
        deciles (deciles/read-csv
                  (data/output-file 
                    "qcost" label "pred.csv.gz"))]
    (assoc record :decilecost (z/mean deciles/cost deciles))))
;;----------------------------------------------------------------
(write-results
  (z/map 
    decile-cost 
    (z/mapcat 
      read-results 
      ["taiga" "randomForestSRC" "quantregForest"])))
;;----------------------------------------------------------------
