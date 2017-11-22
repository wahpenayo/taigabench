(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2017-11-20"
      :date "2017-11-20"
      :doc "[Public airline ontime data for benchmarking](http://stat-computing.org/dataexpo/2009/).
            <p>
            <b>Note:</b> this version of the data is used
            for the regression benchmarks. 
            A different version is used for classification,     
            for consistency with 
            [BENCHMARKING RANDOM FOREST IMPLEMENTATIONS](http://datascience.la/benchmarking-random-forest-implementations/)." }
    
    taigabench.ontime.data
  
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

(defn- parse-carrier ^Airline [tuple _]
  (let [^String name (:uniquecarrier tuple)
        ^String name (if (.startsWith name "9") 
                       (str "_" name) 
                       name)]
    (Airline/valueOf Airline name)))

(defn- parse-airport ^Airport [^String airport]
  (Airport/valueOf Airport airport))

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
    (let [i (Integer/parseInt (:dayofmonth tuple))]
      (.get ^java.util.List days (dec i)))))
;;----------------------------------------------------------------
(z/define-datum Ontime
  [^java.time.Month [month parse-month]
   ^float [monthNumber (fn ^double [tuple _] 
                         (parse-double (:month tuple)))]
   ^taigabench.java.ontime.DayOfMonth [dayofmonth parse-dayofmonth]
   ^float [domNumber (fn ^double [tuple _] 
                       (parse-double (:dayofmonth tuple)))]
   ^java.time.DayOfWeek [dayofweek parse-dow]
   ^float [dowNumber  (fn ^double [tuple _] 
                        (parse-double (:dayofweek tuple)))]
   ;; TODO: rotate as periodicity hack?
   ^float [crsdeptime (fn ^double [tuple _] 
                        (parse-double (:crsdeptime tuple)))]
   ^float [crsarrtime (fn ^double [tuple _] 
                        (parse-double (:crsarrtime tuple)))]
   ^float [crselapsedtime (fn ^double [tuple _] 
                            (parse-double (:crselapsedtime tuple)))]
   ^taigabench.java.ontime.Airline [uniquecarrier parse-carrier]
   ^taigabench.java.ontime.Airport [origin 
                                    (fn [tuple _] 
                                      (parse-airport 
                                        (:origin tuple)))]
   ^taigabench.java.ontime.Airport [dest 
                                    (fn [tuple _] 
                                      (parse-airport 
                                        (:dest tuple)))]
   ^float [distance (fn ^double [tuple _] 
                      (parse-double (:distance tuple)))]
   ^float [arrdelay 
           (fn ^double [tuple _] 
             ;; Treating cancelled as 24 hour delay, but should be
             ;; until actual arrival of next available flight.
             (let [cancelled (Integer/parseInt (:cancelled tuple))]
               (if (zero? cancelled)
                 (parse-double (:arrdelay tuple))
                 (* 24.0 60.0))))]
   ^float arrdelayhat])
;;----------------------------------------------------------------
(def attributes 
  "An attribute map for Taiga training/prediction, including
   <code>:ground-truth</code> and <code>:prediction</code>."
  (assoc
    (into {} (map #(vector (keyword (z/name %)) %)
                  [month monthNumber dayofmonth domNumber 
                   dayofweek dowNumber crsdeptime crsarrtime 
                   crselapsedtime uniquecarrier origin dest 
                   distance]))
    :ground-truth arrdelay
    :prediction arrdelayhat))
(def csv-attributes 
  "Attributes to write to sampled data files."
  {:month monthNumber :dayofmonth domNumber :dayofweek dowNumber 
   :crsdeptime crsdeptime :crsarrtime crsarrtime 
   :crselapsedtime crselapsedtime :uniquecarrier uniquecarrier 
   :origin origin :dest dest :distance distance 
   :arrdelay arrdelay})
;;----------------------------------------------------------------
(defn raw-data-file ^java.io.File [year]
  (io/file "data" "ontime" (str year ".csv.bz2")))
(defn read-raw-data ^Iterable  [year]
  (read-tsv-file (raw-data-file year) #"\,"))
(defn data-file ^java.io.File [fname ext]
  (io/file "data" "ontime" (str fname "." ext)))
(defn output-file ^java.io.File [fname ext]
  (let [f (io/file "output" "l2" "ontime" (str fname "." ext))]
    (io/make-parents f)
    f))
(defn results-file ^java.io.File [fname] 
  (let [f (output-file fname ".results.csv")]
    (io/make-parents f)
    f))
;;----------------------------------------------------------------
