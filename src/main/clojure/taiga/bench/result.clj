(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-16"
      :doc "benchmark result datum" }
    
    taiga.bench.result
  
  (:require [zana.api :as z]))
;;------------------------------------------------------------------------------
(z/define-datum Result [^String model
                        ^int ntrain
                        ^int ntest
                        ^float datatime
                        ^float traintime
                        ^float predicttime
                        ^float auc])
;;------------------------------------------------------------------------------
