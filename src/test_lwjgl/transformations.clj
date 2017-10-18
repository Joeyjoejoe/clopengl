(ns test-lwjgl.transformations
  (:use [test-lwjgl.utility])
  (:require [clojure.core.matrix :as m]
	    [thi.ng.math.macros :as mm]))


;; Overide core.matrix with vectorz-clj implementation
(m/set-current-implementation :vectorz) 

(defn scale-matrix [Sx Sy Sz vector]
  (m/diagonal-matrix [Sx Sy Sz 1]))

(defn translate-matrix [Tx Ty Tz]
  (let [translation-matrix (m/mutable (m/identity-matrix 4))
	_ (m/mset! translation-matrix 3 0 Tx)
	_ (m/mset! translation-matrix 3 1 Ty)
	_ (m/mset! translation-matrix 3 2 Tz)]
    translation-matrix))

(defn rotate-x [angle]
  (let [angle (Math/toRadians angle)
	rotation-matrix (m/mutable (m/identity-matrix 4))
	sinus (Math/sin angle)
	cosinus (Math/cos angle)
	_ (m/mset! rotation-matrix 1 1 cosinus)
	_ (m/mset! rotation-matrix 1 2 (- sinus))
	_ (m/mset! rotation-matrix 2 1 sinus)
	_ (m/mset! rotation-matrix 2 2 cosinus)]
    rotation-matrix))

(defn rotate-y [angle]
  (let [angle (Math/toRadians angle)
	rotation-matrix (m/mutable (m/identity-matrix 4))
	sinus (Math/sin angle)
	cosinus (Math/cos angle)
	_ (m/mset! rotation-matrix 0 0 cosinus)
	_ (m/mset! rotation-matrix 0 2 sinus)
	_ (m/mset! rotation-matrix 2 0 (- sinus))
	_ (m/mset! rotation-matrix 2 2 cosinus)]
    rotation-matrix))

(defn rotate-z [angle]
  (let [angle (Math/toRadians angle)
	rotation-matrix (m/mutable (m/identity-matrix 4))
	sinus (Math/sin angle)
	cosinus (Math/cos angle)
	_ (m/mset! rotation-matrix 0 0 cosinus)
	_ (m/mset! rotation-matrix 0 1 (- sinus))
	_ (m/mset! rotation-matrix 1 0 sinus)
	_ (m/mset! rotation-matrix 1 1 cosinus)]
    rotation-matrix))

(defn perspective-projection
  "Returns a perspective transform matrix, which makes far away objects appear
  smaller than nearby objects. The `aspect` argument should be the width
  divided by the height of your viewport and `fov` is the vertical angle
  of the field of view in degrees."
  [fovy aspect near far]
  (let [perspective-matrix (m/mutable (m/identity-matrix 4))
	f (/ (Math/tan (* 0.5 (Math/toRadians fovy))))
	near (- near)
	far (- far)
        nf (/ (- near far))
	_ (m/mset! perspective-matrix 0 0 (/ f aspect))
	_ (m/mset! perspective-matrix 1 1 f)
	_ (m/mset! perspective-matrix 2 2 (mm/addm near far nf))
	_ (m/mset! perspective-matrix 2 3 -1.0)
	_ (m/mset! perspective-matrix 3 2 (mm/mul 2.0 near far nf))]
    perspective-matrix))

(defn position-matrix [x y z]
  (let [position-matrix (m/mutable (m/identity-matrix 4))
	_ (m/mset! position-matrix 0 3 x)
	_ (m/mset! position-matrix 1 3 y)
	_ (m/mset! position-matrix 2 3 z)]
    position-matrix))

(defn look-at
  ([camera] (let [camera @camera
                  position (:position camera)
                  right (:right camera)
                  up (:up camera)
                  direction (:direction camera)]
              (look-at position right up direction)))
  ([position right up direction] (let [position (position-matrix (m/mget position 0) (m/mget position 1) (m/mget position 2))
                                       look (m/mutable (m/identity-matrix 4))
                                       _ (m/mset! look 0 0 (m/mget right 0))
                                       _ (m/mset! look 0 1 (m/mget up 0))
                                       _ (m/mset! look 0 2 (m/mget direction 0))
                                       _ (m/mset! look 1 0 (m/mget right 1))
                                       _ (m/mset! look 1 1 (m/mget up 1))
                                       _ (m/mset! look 1 2 (m/mget direction 1))
                                       _ (m/mset! look 2 0 (m/mget right 2))
                                       _ (m/mset! look 2 1 (m/mget up 2))
                                       _ (m/mset! look 2 2 (m/mget direction 2))]
                                    (m/mmul look position)	
                                   )))

(defn make 
  ([transformation args] 
	(m/as-vector (apply (resolve (symbol (str "test-lwjgl.transformations/" transformation))) args)))
  ([transformation args vectorz] 
	(apply (resolve (symbol (str "test-lwjgl.transformations/" transformation))) args)))
