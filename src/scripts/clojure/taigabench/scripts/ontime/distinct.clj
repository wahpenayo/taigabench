(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-11-03"
      :date "2017-11-21"
      :doc "Determine possible values for enums in
            public airline ontime data benchmark:
            https://www.r-bloggers.com/benchmarking-random-forest-implementations/
            http://stat-computing.org/dataexpo/2009/" }
    
    taigabench.scripts.ontime.distinct
  
  (:require [clojure.string :as s]
            [clojure.pprint :as pp]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taigabench.ontime.data :as data]))
;; clj48g src\scripts\clojure\taigabench\scripts\ontime\distinct.clj > distinct.txt
;;----------------------------------------------------------------
(defn- split [^String s] (s/split s #"\,"))

(defn- record [header tokens]
  (let [r (zipmap header tokens)]
    {:origin (:origin r) 
     :dest (:dest r)
     :uniquecarrier (:uniquecarrier r)}))

(defn- read-csv [year]
  (z/seconds
    (print-str year)
    (with-open [r (z/reader (data/raw-data-file year))]
      (let [lines (line-seq r)
            header (mapv #(keyword (s/lower-case %))
                         (split (first lines)))
            _(pp/pprint header)
            records (mapv #(record header (split %)) (rest lines))]
        (println (count records))
        (pp/pprint (first records))
        records))))
;;----------------------------------------------------------------
(let [data (z/seconds 
             "read train"
             (z/mapcat
               read-csv
               ["2003" "2004" "2005" "2006" "2007" "2007" "2008"]))
      _(pp/pprint (first data))
      carriers (z/sort (z/distinct :uniquecarrier data))
      airports (z/sort (z/union (z/distinct :origin data)
                                (z/distinct :dest data)))]
  (pp/pprint {:records (z/count data) 
              :carriers (z/count carriers)
              :airports (z/count airports)})
  (pp/pprint carriers)
  (pp/pprint airports))
;;----------------------------------------------------------------
