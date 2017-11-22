(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-11-16"
      :date "2017-11-20"
      :doc "simple prediction/truth datum" }
    
    taigabench.pt
  
  (:require [zana.api :as z]))
;;------------------------------------------------------------------------------
(z/define-datum PredictionTruth [^float prediction ^float truth])
;;------------------------------------------------------------------------------
(defn write-predictions [^clojure.lang.IFn$OD truth
                          ^clojure.lang.IFn$OD predict 
                          ^Iterable trpr
                          ^java.io.File file]
  (with-open [w (z/print-writer file)]
    (.println w "truth\tprediction")
    (z/mapc #(do 
               (.print w (.invokePrim truth %))
               (.print w "\t")
               (.println w (.invokePrim predict %)))
            trpr)))
;;------------------------------------------------------------------------------
