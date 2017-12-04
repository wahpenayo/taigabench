(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2017-12-01"
      :doc "Quantile record class, to hold ground truth
            and predicted values for accuracy measurement." }
    
    taigabench.deciles
  
  (:require [clojure.string :as s]
            [zana.api :as z])
  (:import [clojure.lang IFn$ODD]))
;;----------------------------------------------------------------
(deftype Deciles
  [^double truth
   ^double q10
   ^double q20
   ^double q30
   ^double q40
   ^double q50
   ^double q60
   ^double q70
   ^double q80
   ^double q90])
;;----------------------------------------------------------------
(defn make 
  "<dl>
   <dt><code>^double y</code></dt>
   <dd>ground truth to compare to deciles.</dd> 
   <dt><code>quantile</code></dt>
   <dd>a function taking 2 arguments:
   <code>(quantile d p)</code>, 
   where <code>d</code> is a real probability measure,
   and <code>(<= 0.0 p 1.0)</code> is a <code>double</code>.
   <code>quantile</code> returns the <code>p</code>th quantile
   of the distribution represented by <code>d</code>.
   </dd>
   <dt>d</dt>
   <dd>an object which represents a probability measure on 
   <b>R</b>.
   </dd>
   </dl>"
  (^Deciles [^double truth ^IFn$ODD quantile d]
    (Deciles.
      truth
      (quantile d 0.10)
      (quantile d 0.20)
      (quantile d 0.30)
      (quantile d 0.40)
      (quantile d 0.50)
      (quantile d 0.60)
      (quantile d 0.70)
      (quantile d 0.80)
      (quantile d 0.90))))
;;----------------------------------------------------------------
(defn- qcost 
  
  "Quantile regression cost at <code>p</code>,
   with a somewhat unusual scaling:<br>
   the value is <code>(* w (abs (- y q)))</code>, 
   where <code>w</code> is<br>
   <code>(/ 0.5 (- 1.0 p))</code> if <code>(> y q)</code><br>
   <code>(/ 0.5 p)</code> if <code>(< y q)</code>.<br>
   More commonly, one sees:<br>
   <code>(* 0.5 p)</code> if <code>(> y q)</code><br>
   <code>(* 0.5 (- 1 p))</code> if <code>(< y q)</code>.<br>
   The 2 scaling are derived from one another by multiplying
   or dividing by <code>(* p (- 1 p))</code>. My scaling gives
   more weight to the outer quantiles, which are essentially
   ignored by the conventional weighting.
   <p>
   <dl>
   <dt><code>^double y</code></dt>
   <dd> 'Ground truth', supposedly a sample from a distribution
   with <code>p</code>th quantile <code>q</code>.
   </dd>
   <dt>^double q</dt>
   <dd>'Estimate' of the <code>p</code>th quantile of whatever
    uncertainty we have about <code>y</code>.
   </dd>
   <dt>^double p</dt>
   <dd><code>'(<= 0.0 p 1.0)</code> Which quantile are we 
    evaluating.
   </dd>
   </dl>"
  
  ^double [^double y ^double q ^double p]
  (assert (<= 0.0 p 1.0))
  (let [y-q (- y q)]
    (if (<= 0.0 y-q)
      (* 0.5 (/ y-q (- 1.0 p)))
      (* 0.5 (/ y-q (- p))))))
;;----------------------------------------------------------------
(defn cost 
  
  "Quantile regression cost averaged over the deciles,
   with a somewhat unusual scaling:<br>
   the value is <code>(* w (abs (- y q)))</code>, 
   where <code>w</code> is<br>
   <code>(/ 0.5 (- 1.0 p))</code> if <code>(> y q)</code><br>
   <code>(/ 0.5 p)</code> if <code>(< y q)</code>.<br>
   More commonly, one sees:<br>
   <code>(* 0.5 p)</code> if <code>(> y q)</code><br>
   <code>(* 0.5 (- 1 p))</code> if <code>(< y q)</code>.<br>
   The 2 scaling are derived from one another by multiplying
   or dividing by <code>(* p (- 1 p))</code>. My scaling gives
   more weight to the outer quantiles, which are essentially
   ignored by the conventional weighting.   <p>
   <dl>
   <dt><code>^double y</code></dt>
   <dd> 'Ground truth', supposedly a sample from a distribution
   with <code>p</code>th quantile <code>q</code>.
   </dd>
   <dt>d</dt>
   <dd>an object which represents a probability measure on 
   <b>R</b>, currently either an instance of <code>Deciles</code>
   or 
   <code>org.apache.commons.math3.distribution.RealDistribution</code>
   </dd>
   </dl>"
  
  ^double [^Deciles d]
  (let [y (.truth d)]
    (+ (qcost y (.q10 d) 0.10)
       (qcost y (.q20 d) 0.20)
       (qcost y (.q30 d) 0.30)
       (qcost y (.q40 d) 0.40)
       (qcost y (.q50 d) 0.50)
       (qcost y (.q60 d) 0.60)
       (qcost y (.q70 d) 0.70)
       (qcost y (.q80 d) 0.80)
       (qcost y (.q90 d) 0.90))))
;;----------------------------------------------------------------
(defn mean-cost 
  "Quantile regression cost averaged over the deciles.
   See [[cost]]."
  ^double [^Iterable deciles]
  (z/mean cost deciles))
;;----------------------------------------------------------------
(defn write-csv [^Iterable deciles
                 ^java.io.File file]
  (with-open [w (z/print-writer file)]
    (.println w "truth,q10,q20,q30,q40,q50,q60,q70,q80,q90")
    (z/mapc (fn write-line [^Deciles d]
              (.print w (.truth d))
              (.print w ",")
              (.print w (.q10 d))
              (.print w ",")
              (.print w (.q20 d))
              (.print w ",")
              (.print w (.q20 d))
              (.print w ",")
              (.print w (.q30 d))
              (.print w ",")
              (.print w (.q40 d))
              (.print w ",")
              (.print w (.q50 d))
              (.print w ",")
              (.print w (.q60 d))
              (.print w ",")
              (.print w (.q70 d))
              (.print w ",")
              (.print w (.q80 d))
              (.print w ",")
              (.print w (.q90 d))
              (.print w "\n")
            deciles)))
;;----------------------------------------------------------------
(defn- split [^String s] (s/split s #"\,"))

(defn- head [^String line]
  (mapv #(keyword (s/lower-case %)) (split line)))

(defn- readline ^Deciles [header ^String line]
  (let [tokens (split line)
        r (zipmap header tokens)]
    (Deciles.
      (Double/parseDouble (:truth r))
      (Double/parseDouble (:q10 r))
      (Double/parseDouble (:q20 r))
      (Double/parseDouble (:q30 r))
      (Double/parseDouble (:q40 r))
      (Double/parseDouble (:q50 r))
      (Double/parseDouble (:q60 r))
      (Double/parseDouble (:q70 r))
      (Double/parseDouble (:q80 r))
      (Double/parseDouble (:q90 r)))))

(defn read-csv ^Iterable [^java.io.File file]
  (with-open [r (z/reader file)]
    (let [lines (line-seq r)
          header (head (first lines))]
      (z/map #(readline header %) (rest lines)))))
;;----------------------------------------------------------------
