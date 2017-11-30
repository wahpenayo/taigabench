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
    
    taigabench.ontime.data
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]
            [taiga.api :as taiga])
  (:import [java.time DayOfWeek LocalDate Month]
           [clojure.lang IFn$OD]
           [taigabench.java.ontime Airline Airport DayOfMonth]))
;;----------------------------------------------------------------
(defn- parse-double ^double [^String s] 
  (if (= "NA" s)
    Double/NaN
    (Double/parseDouble s)))

(defn- parse-dayofyear 
  "Original data doesn't have `dayofyear`."
  ^double [tuple _]
  (let [doy (:dayofyear tuple)]
    (if doy 
      (parse-double doy)
      ;; assuming no missing data
      (let [y (int (Double/parseDouble (:year tuple)))
            m (int (Double/parseDouble (:month tuple)))
            d (int (Double/parseDouble (:dayofmonth tuple)))]
        (.getDayOfYear (LocalDate/of y m d))))))

(defn- parse-daysaftermar1 
  "Original data doesn't have `daysaftermar1`."
  ^double [tuple _]
  (let [dam1 (:daysaftermar1 tuple)]
    (if dam1 
      (parse-double dam1)
      ;; assuming no missing data
      (let [y (int (Double/parseDouble (:year tuple)))
            m (int (Double/parseDouble (:month tuple)))
            d (int (Double/parseDouble (:dayofmonth tuple)))
            doy (.getDayOfYear (LocalDate/of y m d))
            mar1 (.getDayOfYear (LocalDate/of y 3 1))]
        (mod (- doy mar1) (int 365)))))) 

(defn- parse-cmonth ^Month [tuple _]
  (let [x (:cmonth tuple)]
    (if x
      (Month/valueOf x)
      (Month/of (Integer/parseInt (:month tuple))))))

(defn- parse-cdayofweek ^DayOfWeek [tuple _]
  (let [x (:cdayofweek tuple)]
    (if x
      (DayOfWeek/valueOf x)
      (DayOfWeek/of (Integer/parseInt(:dayofweek tuple))))))

(defn- parse-cdayofmonth ^DayOfMonth [tuple _]
  (let [x (:cdayofmonth tuple)]
    (if x
      (DayOfMonth/valueOf x)
      (DayOfMonth/of (Integer/parseInt (:dayofmonth tuple))))))

(defn- parse-carrier ^Airline [tuple _]
  (let [^String name (:uniquecarrier tuple)
        ^String name (if (.startsWith name "9") 
                       (str "_" name) 
                       name)]
    (Airline/valueOf name)))

(defn- parse-airport ^Airport [^String airport]
  (Airport/valueOf airport))

(defn- parse-arrdelay 
  (^double [tuple]
    ;; Treating cancelled/diverted as 24 hour delay, 
    ;; but should be until actual arrival of next 
    ;; available flight.
    (let [arrdelay (:arrdelay tuple)]
      (if (and arrdelay (not= "NA" arrdelay))
        (parse-double arrdelay)
        (let [cancelled (:cancelled tuple)
              diverted (:diverted tuple)]
          (if (or (not= "0" cancelled) (not= "0" diverted))
            (* 24.0 60.0)
            Double/NaN ;; missing
            #_(throw (IllegalArgumentException.
                      (str "can't parse:\n"
                           (z/pprint-map-str tuple)))))))))
  (^double [tuple _] (parse-arrdelay tuple)))

(defn- parse-arr-delayed-15min ^double [tuple _] 
  (let [delay (parse-arrdelay tuple)]
    (if (< delay 15.0) 
      0.0 
      1.0)))
;;----------------------------------------------------------------
(z/define-datum Ontime
  [^float [month (fn mon ^double [tuple _] 
                   (parse-double (:month tuple)))]
   ^float [dayofmonth (fn dom ^double [tuple _] 
                        (parse-double (:dayofmonth tuple)))]
   ^float [dayofweek  (fn dow ^double [tuple _] 
                        (parse-double (:dayofweek tuple)))]
   ^float [dayofyear parse-dayofyear]
   ^float [daysaftermar1 parse-daysaftermar1]
   ;; TODO: rotate as periodicity hack?
   ^float [crsdeptime (fn dep ^double [tuple _] 
                        (parse-double (:crsdeptime tuple)))]
   ^float [crsarrtime (fn arr ^double [tuple _] 
                        (parse-double (:crsarrtime tuple)))]
   ^float [crselapsedtime (fn elapsed ^double [tuple _] 
                            (let [elapsed 
                                  (parse-double 
                                    (:crselapsedtime tuple))]
                              (if (Double/isNaN elapsed)
                                ;; NOTE: NOT CORRECT!
                                ;; scheculed times are in local 
                                ;; zones, need to convert  to UTC, 
                                ;; which requires timezone
                                ;; per airport, and correct DST.
                                ;; Should missing scheduled 
                                ;; elapsed times be 
                                ;; dropped meanwhile?
                                (- (parse-double 
                                     (:crsarrtime tuple))
                                   (parse-double 
                                     (:crsdeptime tuple)))
                                elapsed)))]
   ^float [distance (fn dist ^double [tuple _] 
                      (parse-double (:distance tuple)))]
   ^java.time.Month [cmonth parse-cmonth]
   ^taigabench.java.ontime.DayOfMonth [cdayofmonth parse-cdayofmonth]
   ^java.time.DayOfWeek [cdayofweek parse-cdayofweek]
   ^taigabench.java.ontime.Airline [uniquecarrier parse-carrier]
   ^taigabench.java.ontime.Airport [origin 
                                    (fn origin [tuple _] 
                                      (parse-airport 
                                        (:origin tuple)))]
   ^taigabench.java.ontime.Airport [dest 
                                    (fn dest [tuple _] 
                                      (parse-airport 
                                        (:dest tuple)))]
   ^float [arrdelay parse-arrdelay]
   ^float arrdelayhat
   ^float [arr-delayed-15min parse-arr-delayed-15min]
   ^float score])
;;----------------------------------------------------------------
(def predictors 
  "An attribute map for Taiga training/prediction."
  (into {} (map #(vector (keyword (z/name %)) %)
                [month dayofmonth dayofweek dayofyear daysaftermar1
                 crsdeptime crsarrtime crselapsedtime distance
                 cmonth cdayofmonth cdayofweek
                 uniquecarrier origin dest])))
(def l2-attributes 
  "An attribute map for Taiga training/prediction, including
   <code>:ground-truth</code> and <code>:prediction</code>."
  (assoc
    predictors
    :ground-truth arrdelay
    :prediction arrdelayhat))
(def classify-attributes 
  "An attribute map for Taiga training/prediction, including
   <code>:ground-truth</code> and <code>:prediction</code>."
  (assoc
    predictors
    :ground-truth arr-delayed-15min
    :prediction score))
(def csv-attributes 
  "A list of attributes to write to sampled data files."
  (sort-by 
    z/name 
    (conj (vals predictors) arrdelay)))
;;----------------------------------------------------------------
(defn raw-data-file ^java.io.File [year]
  (io/file "data" "ontime" (str year ".csv.bz2")))
(defn read-raw-data ^Iterable  [year]
  (read-tsv-file (raw-data-file year) #"\,"))
;;----------------------------------------------------------------
;; TODO: fix z/define-datum io so round trip csv <-> data 
;; consistency is easy to ensure, also save writing and reading
;; redundant attributes (categorial and numerical).
(defn- data-file ^java.io.File [fname ext]
  (io/file "data" "ontime" (str fname "." ext)))
(def ^{:private true :tag String} csv-header 
  (s/join "," (map z/name csv-attributes)))
(defn- to-string 
  "The `float` valued attributes should really be `int`
   (except for the prediction attributes, which aren't written)."
  [x r]
  (str
    (if (instance? IFn$OD x)
      (int (.invokePrim ^IFn$OD x r))
      (x r)
      #_(let [xr (x r)]
          (cond ;;(instance? Airline xr) xr
                ;;(instance? Airport xr) xr
                (instance? DayOfMonth xr) (.getValue ^DayOfMonth xr)
                (instance? DayOfWeek xr) (.getValue ^DayOfWeek xr)
                (instance? Month xr) (.getValue ^Month xr)
                :else xr)))))
(defn- csv-line ^String [^Ontime r]
  (s/join "," (mapv str (mapv #(to-string % r) csv-attributes))))
(defn write-data-file [^Iterable data ^String prefix]
  (with-open [w (z/print-writer (data-file prefix "csv.gz"))]
    (.println w csv-header)
    (z/mapc #(.println w (csv-line %)) data)))
(defn read-data-file ^Iterable [^String prefix]
  (read-tsv-file (data-file prefix "csv.gz") #"\,"))
;;----------------------------------------------------------------
(defn output-file ^java.io.File [problem fname ext]
  (let [f (io/file "output" problem "ontime" (str fname "." ext))]
    (io/make-parents f)
    f))
(defn results-file ^java.io.File [problem fname] 
  (let [f (output-file fname "results.csv")]
    (io/make-parents f)
    f))
;;----------------------------------------------------------------
