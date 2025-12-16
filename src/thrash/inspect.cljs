(ns thrash.inspect
  "Higher level functions that inspect combinations of maps and entities"
  (:require [thrash.util :as util]
            [thrash.map :as map]
            [thrash.entity :as ent]))

(defn description-of-coords [map mobs x y]
  (let [map-at-coords ((util/encode-coords x y) map)
        mob-at-coords (filter (partial util/is-at-coords? x y) mobs)]
    (if (seq mob-at-coords)
      (ent/describe-entity (first mob-at-coords))
      (map/describe-terrain map-at-coords))))

(defn description-of-terrain [map x y]
  (let [map-at-coords ((util/encode-coords x y) map)]
    (map/describe-terrain map-at-coords)))

(defn move-allowed? [map mobs x y]
  (let [encoded-coords (util/encode-coords x y)]
    (and
     (contains? map encoded-coords)
     (map/passable? map {:x x :y y})
     (not (ent/mob-at-xy? mobs x y)))))

(defn glyph-at-coords [map mobs x y]
  (let [map-tile ((util/encode-coords x y) map)
        glyph-at-coords (and map-tile (:glyph map-tile))
        mob-at-coords (filter (partial util/is-at-coords? x y) mobs)]
    (if (seq mob-at-coords)
      (:glyph (first mob-at-coords))
      glyph-at-coords)))