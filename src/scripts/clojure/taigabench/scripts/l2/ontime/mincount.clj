(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-01-18"
      :doc "sweep mincount values.
            Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.l2.ontime.mincount
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.ontime.data :as data]
            [taigabench.l2.ontime.traintest :as l2]))
;; clj12g src\scripts\clojure\taigabench\scripts\l2\ontime\mincount.clj > ontime.mincount.txt
;; clj48g src\scripts\clojure\taigabench\scripts\l2\ontime\mincount.clj > ontime.mincount.txt
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
(def mincounts [7 15 31 63 127 255 511 1023 2047 4095])
(def suffix "2097152")
;;----------------------------------------------------------------
(defn reducer [records ^long mincount]
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
      (data/output-file "l2-mincount" "taiga.results" "csv"))
    records))
;;----------------------------------------------------------------
(reduce reducer [] mincounts)
;;----------------------------------------------------------------
