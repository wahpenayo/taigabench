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
;; clj48g src\scripts\clojure\taigabench\scripts\ontime\sample.clj 
;; clj12g src\scripts\clojure\taigabench\scripts\ontime\sample.clj 
;;----------------------------------------------------------------
(def ^Random prng (z/mersenne-twister-generator 
                     "8444A935C2629BA47DF20FD62F69CF8E"))
(let [data (z/seconds "read test" (data/read-raw-data "2008"))
      n (* 1024 1024)
      test (z/seconds 
            (print-str "sample" n "from" (z/count data))
            (z/sample prng n data))]
   (z/seconds 
     (print-str (z/count test))
     (data/write-data-file test "test")))

;; TODO: sample so that smaller sets are subsets of larger?
#_(let [data (z/seconds 
              "read train"
              (z/mapcat data/read-raw-data 
                        ["2003" "2004" "2005" "2006" "2007"]))]
   (doseq [^long n (take 5 (iterate (partial * 8) (* 8 8 8 8 2)))]
     (let [train
           (z/seconds 
             (print-str "sample" n "from" (z/count data))
             (z/sample prng n data))]
       (z/seconds
         (print-str "write" n (z/count train))
         (data/write-data-file train (str "train-" n))))))
;;----------------------------------------------------------------
