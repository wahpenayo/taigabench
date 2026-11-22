(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2026-11-21"
      :doc "Public airline ontime data for benchmarking:
            http://stat-computing.org/dataexpo/2009/" }

    taigabench.l2.ontime.traintest

  (:require
   [taigabench.ontime.data :as data]
   [taigabench.pt :as pt]
   [zana.api :as z]))
;;----------------------------------------------------------------
(def prototype (assoc data/prototype
                      :attributes data/l2-attributes))
;;----------------------------------------------------------------
(defn traintest [suffix learner options]
  (let [_mincount (:mincount options)
        model-name (str "taiga-" (z/name learner))
        label  (str "taiga-" suffix)
        start (System/nanoTime)
        train (data/read-data-file (str "train-" suffix))
        _(println "train:" (z/count train))
        test (data/read-data-file (:test options "test"))
        _(println "test:" (z/count test))
        _(System/gc)
        datatime (/ (double (- (System/nanoTime) start))
                    1000000000.0)
        options (assoc options :data train)
        start (System/nanoTime)
        ^clojure.lang.IFn$OOD model (learner options)
        traintime (/ (double (- (System/nanoTime) start))
                     1000000000.0)
        start (System/nanoTime)
        attributes (:attributes options)
        test (z/pmap #(assoc
                        %
                        :prediction (.invokePrim model attributes %))
                     test)
        predicttime (/ (double (- (System/nanoTime) start))
                       1000000000.0)
        start (System/nanoTime)
        ^clojure.lang.IFn$OD truth (:ground-truth attributes)
        ^clojure.lang.IFn$OD prediction (:prediction attributes)
        rmse (z/rms-difference truth prediction test)
        rmsetime (/ (double (- (System/nanoTime) start))
                    1000000000.0)]
    (pt/write-predictions
      truth prediction test
      (data/output-file "l2" label "pred.csv.gz"))
  {:model model-name
   :ntrain (z/count train)
   :ntest (z/count test)
   :datatime datatime
   :traintime traintime
   :predicttime predicttime
   :rmsetime rmsetime
   :rmse rmse
   :mincount (:mincount options)
   :maxdepth (:maxdepth options)
   :nterms (:nterms options)}))
;;----------------------------------------------------------------
