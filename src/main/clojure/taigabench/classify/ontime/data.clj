(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-11-10"
      :date "2017-11-27"
      :doc "[Public airline ontime data for benchmarking](http://stat-computing.org/dataexpo/2009/).
            <p>
            <b>Note:</b>Using attributes mostly as defined as in 
            [BENCHMARKING RANDOM FOREST IMPLEMENTATIONS](http://datascience.la/benchmarking-random-forest-implementations/):
            a subset of possible predictors and departure delay
            greater than 15 minutes as the outcome.
            <p>
            The [original benchmark data sampling code](https://github.com/szilard/benchm-ml/blob/master/0-init/2-gendata.txt)
            appears to use actual departure time as a predictor,
            which would make the model useless for predicting
            departure delay, since if you know the actual departure 
            time and the scheduled departure time, you can 
            compute the delay without any model.
            See [Issue 33](https://github.com/szilard/benchm-ml/issues/33).
            <p>
            Here, I've replaced `DepTime` (actual departure time,
            unknownable when a prediction would be useful)
            by `CRSDepTime`, the scheduled departure time.
            <p> 
            I've added scheduled arrival and elasped times as
            predictors." }
    
    taigabench.classify.ontime.data
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga])
  (:import [java.time DayOfWeek Month]
           [taigabench.java.ontime Airline Airport DayOfMonth]))
;;----------------------------------------------------------------
(defn- parse-double ^double [^String s] 
  (if (= "NA" s)
    Double/NaN
    (Double/parseDouble s)))

(defn- parse-month ^Month [tuple _]
  (Month/of (Integer/parseInt (:month tuple))))

(defn- parse-dow ^DayOfWeek [tuple _]
  (DayOfWeek/of (Integer/parseInt(:dayofweek tuple))))

(defn- parse-dayofmonth ^DayOfMonth [tuple _]
  (DayOfMonth/of (Integer/parseInt (:dayofmonth tuple))))

(defn- parse-carrier ^Airline [tuple _]
  (let [^String name (:uniquecarrier tuple)
        ^String name (if (.startsWith name "9") 
                       (str "_" name) 
                       name)]
    (Airline/valueOf Airline name)))

(defn- parse-airport ^Airport [^String airport]
  (Airport/valueOf Airport airport))
;;----------------------------------------------------------------
(z/define-datum Ontime
  [^float [month (fn ^double [tuple _] 
                   (parse-double (:month tuple)))]
   ^float [dayofmonth (fn ^double [tuple _] 
                        (parse-double (:dayofmonth tuple)))]
   ^float [dayofweek  (fn ^double [tuple _] 
                        (parse-double (:dayofweek tuple)))]
   ^float [dayofyear (fn ^double [tuple _] 
                       (parse-double (:dayofyear tuple)))]
   ^float [daysaftermar1 (fn ^double [tuple _] 
                           (parse-double (:daysaftermar1 tuple)))]
   ;; TODO: rotate as periodicity hack?
   ^float [crsdeptime (fn ^double [tuple _] 
                        (parse-double (:crsdeptime tuple)))]
   ^float [crsarrtime (fn ^double [tuple _] 
                        (parse-double (:crsarrtime tuple)))]
   ^float [crselapsedtime (fn ^double [tuple _] 
                            (let [elapsed 
                                  (parse-double 
                                    (:crselapsedtime tuple))]
                              (if (Double/isNaN elapsed)
                                (- (parse-double 
                                     (:crsarrtime tuple))
                                   (parse-double 
                                     (:crsdeptime tuple)))
                                elapsed)))]
   ^float [distance (fn ^double [tuple _] 
                      (parse-double (:distance tuple)))]
   ^java.time.Month [cmonth parse-month]
   ^taigabench.java.ontime.DayOfMonth [cdayofmonth parse-dayofmonth]
   ^java.time.DayOfWeek [cdayofweek parse-dow]
   ^taigabench.java.ontime.Airline [uniquecarrier parse-carrier]
   ^taigabench.java.ontime.Airport [origin 
                                    (fn [tuple _] 
                                      (parse-airport 
                                        (:origin tuple)))]
   ^taigabench.java.ontime.Airport [dest 
                                    (fn [tuple _] 
                                      (parse-airport 
                                        (:dest tuple)))]
   ^float [arr-delayed-15min 
           (fn ^double [tuple _] 
             (let [yn (:arr-delayed-15min tuple)]
               (case yn
                 "Y" (double 1.0)
                 "N" (double 0.0)
                 Double/NaN)))]
   ^float score])
;;----------------------------------------------------------------
(def attributes 
  "An attribute map for Taiga training/prediction, including
   <code>:ground-truth</code> and <code>:prediction</code>."
  (assoc
    (into {} (map #(vector (keyword (z/name %)) %)
                  [month dayofmonth dayofweek dayofyear daysaftermar1
                   crsdeptime crsarrtime crselapsedtime distance
                   cmonth cdayofmonth cdayofweek
                   uniquecarrier origin dest]))
    :ground-truth arr-delayed-15min
    :prediction score))
;;----------------------------------------------------------------
(defn data-file ^java.io.File [fname ext]
  (io/file "data" "classify" "ontime" 
           (str fname "." ext)))
(defn output-file ^java.io.File [fname ext]
  (let [f (io/file "output" "classify" "ontime"
                   (str fname "." ext))]
    (io/make-parents f)
    f))
(defn results-file ^java.io.File [fname mincount] 
  (let [f (output-file fname ".results.csv")]
    (io/make-parents f)
    f))
;;----------------------------------------------------------------
