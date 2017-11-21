(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-11-10"
      :date "2017-11-20"
      :doc "Public airline ontime data for benchmarking:
            http://stat-computing.org/dataexpo/2009/" }
    
    taiga.bench.classify.ontime.data
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga]
            [taiga.bench.classify.metrics :as metrics])
  (:import [java.time DayOfWeek Month]
           [taigabench.java.ontime Airline Airport DayOfMonth]))
;;----------------------------------------------------------------
(defn- strip-quotes ^String [^String s] 
  (.replaceAll s "[\"]+" ""))

(defn- parse-int ^double [^String s] 
  (double (Integer/parseInt (strip-quotes s))))

(defn- parse-month ^Month [tuple _]
  (Month/of 
    (Integer/parseInt 
      (.substring (strip-quotes (:month tuple)) 2))))

(defn- parse-dow ^DayOfWeek [tuple _]
  (DayOfWeek/of 
    (Integer/parseInt 
      (.substring (strip-quotes (:dayofweek tuple)) 2))))

(defn- parse-carrier ^Airline [tuple _]
  (let [^String name (strip-quotes (:uniquecarrier tuple))
        ^String name (if (.startsWith name "9") (str "_" name) name)]
    (Airline/valueOf Airline name)))

(defn- parse-airport ^Airport [^String airport]
  (Airport/valueOf Airport (strip-quotes airport)))

(let [days [DayOfMonth/TSUITACHI DayOfMonth/FUTSUKA 
            DayOfMonth/MIKKA DayOfMonth/YOKKA DayOfMonth/ITSUKA 
            DayOfMonth/MUIKA DayOfMonth/NANOKA DayOfMonth/YOUKA 
            DayOfMonth/KOKONOKA DayOfMonth/DOOKA 
            DayOfMonth/JUUICHINICHI DayOfMonth/JUUNINICHI  
            DayOfMonth/JUUSANNICHI DayOfMonth/JUUYONNICHI 
            DayOfMonth/JUUGONICHI DayOfMonth/JUUROKUNICHI 
            DayOfMonth/JUUSHICHINICHI DayOfMonth/JUUHACHINICHI 
            DayOfMonth/JUUKUNICHI DayOfMonth/HATSUKA 
            DayOfMonth/NIJUUICHINICHI DayOfMonth/NIJUUNINICHI  
            DayOfMonth/NIJUUSANNICHI DayOfMonth/NIJUUYONNICHI 
            DayOfMonth/NIJUUGONICHI DayOfMonth/NIJUUROKUNICHI
            DayOfMonth/NIJUUSHICHINICHI DayOfMonth/NIJUUHACHINICHI
            DayOfMonth/NIJUUKUNICHI DayOfMonth/SANJUUNICHI
            DayOfMonth/SANJUUICHINICHI]]
  (defn- parse-dayofmonth ^DayOfMonth [tuple _]
    (let [^String s (strip-quotes (:dayofmonth tuple))
          i (- (Integer/parseInt (.substring s (int 2))) (int 1))]
      (.get ^java.util.List days i))))
;;----------------------------------------------------------------
(z/define-datum Ontime
  [^java.time.Month 
   [month parse-month]
   ^taigabench.java.ontime.DayOfMonth 
   [dayofmonth parse-dayofmonth]
   ^java.time.DayOfWeek 
   [dayofweek parse-dow]
   ^float
   [deptime (fn ^double [tuple _] (parse-int (:deptime tuple)))]
   ^taigabench.java.ontime.Airline 
   [uniquecarrier parse-carrier]
   ^taigabench.java.ontime.Airport 
   [origin (fn [tuple _] (parse-airport (:origin tuple)))]
   ^taigabench.java.ontime.Airport 
   [dest (fn [tuple _] (parse-airport (:dest tuple)))]
   ^float 
   [distance (fn ^double [tuple _] (parse-int (:distance tuple)))]
   ^float 
   [dep-delayed-15min 
    (fn ^double [tuple _] 
      (let [yn (strip-quotes (:dep-delayed-15min tuple))]
        (case yn
          "Y" (double 1.0)
          "N" (double 0.0))))]
   ^float score])
;;----------------------------------------------------------------
(def attributes 
  "An attribute map for Taiga training/prediction, including
   <code>:ground-truth</code> and <code>:prediction</code>."
  (assoc
    (into {} (map #(vector (keyword (z/name %)) %)
                  [month dayofmonth dayofweek deptime 
                   uniquecarrier origin dest distance]))
    :ground-truth dep-delayed-15min
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
