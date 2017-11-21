(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author ["wahpenayo at gmail dot com"
               "John Alan McDonald"
               "Kristina Lisa Klinkner" ]
      :since "2016-11-15"
      :date "2017-11-20"
      :doc "Accuracy metrics designed for permutation importance measures." }
    
    taiga.bench.classify.metrics
  
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [zana.api :as z]))
;;------------------------------------------------------------------------------
(def ^{:private true :tag java.util.Random} auc-prng
  (z/mersenne-twister-generator "C589FB216CD402EC0A711C04E40801BE"))
;;------------------------------------------------------------------------------
(defn roc-auc
  
  "A [metric function](1metrics.html).
   Returns AUC, the area under the precision/recall curve for a classification
   model. https://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U_test
   <dl>
   <dt><code>model</code></dt>
   <dd>a <code>clojure.lang.IFn$OOD</code> that takes an attribute map and a
   datum, one of the elements of <code>data</code>.
   </dd>
   <dt><code>attributes</code></dt><dd>a <code>java.util.Map</code> from
   keywords to functions that can be applied to the elements of 
   <code>data</code>. Must contain <code>:ground-truth</code> and 
   <code>:prediction</code> as a keys.
   </dd>
   <dt><code>data</code></dt><dd>an <code>Iterable</code></dd>
   </dl>"
  
  ^double [^clojure.lang.IFn$OD truth
           ^clojure.lang.IFn$OD prediction 
           ^Iterable data]
  
  (let [;; shuffle to avoid handling tied predictions
        ;; TODO: sort in place, or shuffle/sort iterators
        it (z/iterator (z/sort-by prediction (z/shuffle data auc-prng)))]
    (loop [i (int 1)
           n0 (int 0)
           r0 (double 0.0)]
      (if (z/has-next? it)
        (let [datum (z/next-item it)
              t (.invokePrim truth datum)]
          (cond (== 0.0 t) (recur (inc i) (inc n0) (+ r0 i))
                (== 1.0 t) (recur (inc i) n0 r0)
                :else (throw 
                        (IllegalArgumentException.
                          (print-str "Ground truth isn't 0.0 or 1.0:"
                                     t "\n" datum)))))
        (let [u0 (- r0 (* 0.5 n0 (inc n0)))
              n1 (- (z/count data) n0)
              u1 (- (* n0 n1) u0)]
          (/ (Math/max u0 u1) (* n0 n1)))))))
;;------------------------------------------------------------------------------

