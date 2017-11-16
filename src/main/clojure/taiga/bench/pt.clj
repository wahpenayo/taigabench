(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-16"
      :doc "simple prediction/truth datum" }
    
    taiga.bench.pt
  
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
