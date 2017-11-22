(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2017-11-21"
      :date "2017-11-21"
      :doc "Generate train/test datasets to be used in benchmarking from
            [Public airline ontime data for benchmarking](http://stat-computing.org/dataexpo/2009/).
            <p>
            <b>Note:</b> this version of the data is used
            for the regression benchmarks. 
            A different version is used for classification,     
            for consistency with 
            [BENCHMARKING RANDOM FOREST IMPLEMENTATIONS](http://datascience.la/benchmarking-random-forest-implementations/)." }
    
    taigabench.scripts.ontime.sample
  
  (:require [zana.api :as z]
            [taigabench.ontime.data :as data])
  (:import [java.util Random]))
;; clj9 src\scripts\clojure\taigabench\scripts\ontime\sample.clj 
;;----------------------------------------------------------------
(let [test (z/seconds "read test" (data/read-raw-data "2008"))]
  (println (z/count test))
  (z/seconds "write test"
             (z/write-tsv-file
               data/csv-attributes test
               (data/data-file "test" "csv.gz") ",")))

(def ^Random prng (z/mersenne-twister-generator 
                    "8444A935C2629BA47DF20FD62F69CF8E"))
#_(let [train 
       (z/seconds 
         "read train"
                  (z/mapcat data/read-raw-data 
                            ["2005" "2006" "2007"]))]
   )