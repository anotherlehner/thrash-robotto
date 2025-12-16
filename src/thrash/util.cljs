(ns thrash.util
  "Utility functions used by many other modules"
  (:require [clojure.string :as str]))

(defn todo [keyword] (js/console.log "TODO:" (str keyword)))

(defn log [& params] (apply js/console.log params))

(defn encode-coords
  ([coords] (keyword (str (:x coords) "_" (:y coords))))
  ([x y] (keyword (str x "_" y))))

(defn decode-coords [keyword]
  (map js/parseInt (str/split (subs (str keyword) 1) "_")))

(defn is-at-coords? [x y movable]
  (and (= (:x movable) x) (= (:y movable) y)))

(defn distance [x1 y1 x2 y2]
  (Math/floor (Math/sqrt (+ (Math/pow (- x2 x1) 2) (Math/pow (- y2 y1) 2)))))

(defn coords-equal? [c1 c2]
  (and (= (:x c1) (:x c2)) (= (:y c1) (:y c2))))

(defn xy-equal? [x1 y1 x2 y2]
  (and (= x1 x2) (= y1 y2)))