(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-28"
      :doc "Compute summary statistics for sanity checking." }
    
    taiga.bench.summary
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]))
;;------------------------------------------------------------------------------ 
(defn- distinct-missing-summary [z data]
  (let [n (z/count data)
        not-missing (z/drop-missing z data)
        distinct (z/count (z/distinct z not-missing))
        not-missing (z/count not-missing)
        missing-fraction (if (zero? n) 
                           0.0 
                           (- 1.0 (double (/ not-missing n))))]
    {:n n
     :not-missing not-missing
     :distinct distinct
     :missing-fraction missing-fraction}))

(defn- numerical-summary [z data]
  (if (z/numerical? z data)
    (let [data (z/drop-missing z data)
          ps [0.0 0.10 0.25 0.50 0.75 0.90 1.0]]
      (merge {:is-numerical true}
             (when-not (empty? data)
               (zipmap (mapv #(format "p%03d" (int (* 100.0 (double %))))  ps)
                       (z/quantiles (z/select-finite-values z data) ps)))))
    {:is-numerical false}))

(defn- vector-summary [z data]
  (if (z/vector? z data)
    (let [data (z/drop-missing z data)
          ps [0.0 0.10 0.25 0.50 0.75 0.90 1.0]]
      (merge {:is-vector true}
             (when-not (empty? data)
               (zipmap (mapv #(format "p%03d" (int (* 100.0 (double %))))  ps)
                       (z/quantiles (z/select-finite-values z data) ps)))))
    {:is-vector false}))

(defn- attribute-summary [z data]
  (merge {:attribute (z/name z)}
         (distinct-missing-summary z data) 
         (numerical-summary z data)
         (vector-summary z data)))
;;------------------------------------------------------------------------------ 
(defn- summary [options]
  (println (:type options) (z/count (:data options)))
  (when-not (z/empty? (:data options))
    (let [data (:data options)
          attribute-map (:attributes options)
          options (dissoc options :data :attributes)]
      (z/pmap (fn [[k v]] (merge options (attribute-summary v data)))
              (sort-by key attribute-map)))))

(defn- write-records [records ^java.io.File file]
  (let [header (sort-by name (into #{} (mapcat keys records)))]
    (with-open [w (z/print-writer file)]
      (.println w (s/join "\t" (mapv name header)))
      (doseq [record records]
        (.println w (s/join "\t" (mapv #(get record %) header))))))
  (z/echo "wrote" (z/count records) "to" (z/pathname file)))
;;------------------------------------------------------------------------------   
(defn- check [options]
  (assert (string? (:feature-set options))
          (str "No :feature-set name given:\n"
               (z/pprint-map-str options)))
  (assert (and (instance? java.util.Map (:report-attributes options))
               (not (z/empty? (:report-attributes options)))) 
          (str "Invalid :report-attributes in:\n"
               (z/pprint-map-str options))))
;;------------------------------------------------------------------------------ 
(defn report [options ^java.io.File f]
  (check options)
  (z/seconds
    (print-str "report")
    (let [records (concat 
                    (summary {:attributes (:report-attributes options)
                              :data (:train-data options) :type "train"})
                    (summary {:attributes (:report-attributes options)
                              :data (:test-data options) :type "test"})
                    (summary {:attributes (:report-attributes options)
                              :data (:predict-data options) :type "predict"}))]
      (when-not (z/empty? records)
        (io/make-parents f)
        (write-records (z/sort-by #(vector (:attribute %) (:type %)) records) f)))))
;;------------------------------------------------------------------------------ 
