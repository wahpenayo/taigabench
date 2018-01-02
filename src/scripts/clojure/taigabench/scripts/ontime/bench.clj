(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2017-12-23"
      :doc "Public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.ontime.bench
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.ontime.data :as data]
            [taigabench.classify.ontime.traintest :as cl]
            [taigabench.l2.ontime.traintest :as l2]
            [taigabench.qcost.ontime.traintest :as qcost]))
;; clj12g src\scripts\clojure\taigabench\scripts\ontime\bench.clj > ontime.bench.txt
;; clj48g src\scripts\clojure\taigabench\scripts\ontime\bench.clj > ontime.bench.txt
;; clj56g src\scripts\clojure\taigabench\scripts\ontime\bench.clj > ontime.bench.txt
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
(def suffixes 
  ["32768" "131072" "524288" "2097152" "8388608" "33554432"])
;;----------------------------------------------------------------
#_(loop  [records []
         suffixes suffixes]
   (when-not (empty? suffixes)
     (System/gc)
     (let [suffix (first suffixes)
           record (z/seconds
                    (print-str "taiga" suffix)
                    (l2/traintest 
                      suffix 
                      taiga/mean-regression 
                      l2/prototype))
           records (conj records (assoc record :model "taiga"))]
       (write-csv 
         records 
         (data/output-file "l2" "taiga.results" "csv"))
       (recur records (rest suffixes)))))
;;----------------------------------------------------------------
#_(loop  [records []
         suffixes suffixes]
   (when-not (empty? suffixes)
     (System/gc)
     (let [suffix (first suffixes)
           mvp (assoc 
                 (z/seconds
                   (print-str "taiga-mvp" suffix)
                   (cl/traintest 
                     suffix 
                     taiga/majority-vote-probability 
                     cl/prototype))
                 :model "taiga-mvp")
           pfp (assoc 
                 (z/seconds
                   (print-str "taiga-pfp" suffix)
                   (cl/traintest 
                     suffix 
                     taiga/positive-fraction-probability 
                     cl/prototype))
                 :model "taiga-pfp")
           records (concat records [mvp pfp])]
       (write-csv 
         records 
         (data/output-file "classify" "taiga.results" "csv"))
       (recur records (rest suffixes)))))
;;----------------------------------------------------------------
(loop  [records []
        suffixes suffixes]
  (when-not (empty? suffixes)
    (System/gc)
    (let [suffix (first suffixes)
          record (z/seconds
                   (print-str "taiga" suffix)
                   (qcost/traintest 
                     suffix 
                     taiga/real-probability-measure
                     qcost/prototype))
          records (conj records (assoc record :model "taiga"))]
      (write-csv 
        records 
        (data/output-file "qcost" "taiga.results" "csv"))
      (recur records (rest suffixes)))))
;;----------------------------------------------------------------
