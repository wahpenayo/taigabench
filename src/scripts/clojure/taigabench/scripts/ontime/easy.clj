(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-01-16"
      :doc "Filter test data so that only categories in all the
            training sets are in the test sets.
            [Public airline ontime data for benchmarking](http://stat-computing.org/dataexpo/2009/).
            <p>
            <b>Note:</b> this version of the data is different 
            from that used in
            [BENCHMARKING RANDOM FOREST IMPLEMENTATIONS]
            (http://datascience.la/benchmarking-random-forest-implementations/)." }
    
    taigabench.scripts.ontime.easy
  
  (:require [clojure.set :as set]
            [clojure.pprint :as pp]
            [zana.api :as z]
            [taigabench.ontime.data :as data])
  (:import [java.util Random Set]))
;; clj48g src\scripts\clojure\taigabench\scripts\ontime\easy.clj 
;; clj12g src\scripts\clojure\taigabench\scripts\ontime\easy.clj > easy-data.txt
;;----------------------------------------------------------------
(defn- common-carriers ^Set [^Set cats0 ^String suffix] 
  (let [data (data/read-data-file (str "train-" suffix))
        cats1 (z/distinct data/uniquecarrier data)]
    (if (z/empty? cats0)
      cats1
      (z/intersection cats0 cats1))))
;;----------------------------------------------------------------
(defn- common-origins ^Set [^Set cats0 ^String suffix] 
  (let [data (data/read-data-file (str "train-" suffix))
        cats1 (z/distinct data/origin data)]
    (if (z/empty? cats0)
      cats1
      (z/intersection cats0 cats1))))
;;----------------------------------------------------------------
(defn- common-dests ^Set [^Set cats0 ^String suffix]
  (let [data (data/read-data-file (str "train-" suffix))
        cats1 (z/distinct data/dest data)]
    (if (z/empty? cats0)
      cats1
      (z/intersection cats0 cats1))))
;;----------------------------------------------------------------
(let [suffixes 
      (reverse 
        ["32768" "131072" "524288" "2097152" "8388608" "33554432"])
      ^Set carriers (reduce common-carriers #{} suffixes)
      _(pp/pprint (into #{} carriers))
      ^Set origins (reduce common-origins #{} suffixes)
      _(pp/pprint (into #{} origins))
      ^Set destinations (reduce common-dests #{} suffixes)
      _(pp/pprint (into #{} destinations))
     test (data/read-data-file "test")
      easy (z/filter 
             #(and 
                (.contains carriers (data/uniquecarrier %))
                (.contains origins (data/origin %))
                (.contains destinations (data/dest %)))
             test)]
  (println "test:" (z/count test) "easy:" (z/count easy))
  (data/write-data-file easy "easy"))
;;----------------------------------------------------------------
