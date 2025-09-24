(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-01-20"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.l2.ontime.bench
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.ontime.data :as data]
            [taigabench.l2.ontime.traintest :as l2]))
;; clj12g src\scripts\clojure\taigabench\scripts\l2\ontime\bench.clj > ontime.bench.txt
;; .\clj48g src\scripts\clojure\taigabench\scripts\l2\ontime\bench.clj > ontime.bench.txt
;;----------------------------------------------------------------
(defn- write-csv [^java.util.List records ^java.io.File f]
  (io/make-parents f)
  (let [ks (sort (into #{} (mapcat keys records)))]
    (with-open [w (z/print-writer f)]
      (binding [*out* w]
        (println (s/join "," (map name ks)))
        (doseq [record records]
          (println 
            (s/join "," (map #(or (str (get record %)) "") ks))))
        (flush)))))
;;----------------------------------------------------------------
(def mincount 255)
(def suffixes 
  ["32768" "131072" "524288" "2097152" "8388608" "33554432"])
;;----------------------------------------------------------------
(defn reducer [records suffix]
  (System/gc)
  (let [record (z/seconds
                 (print-str "taiga" suffix)
                 (l2/traintest 
                   suffix 
                   taiga/mean-regression 
                   (assoc l2/prototype :mincount mincount)))
        records (conj records (assoc record :model "taiga"))]
    (write-csv 
      records 
      (data/output-file "l2" "taiga.results" "csv"))
    records))
;;----------------------------------------------------------------
(reduce reducer [] suffixes)
;;----------------------------------------------------------------
