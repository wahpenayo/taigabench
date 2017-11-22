(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :since "2016-11-16"
      :date "2017-11-20"
      :doc "benchmark result datum" }
    
    taigabench.classify.result
  
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
