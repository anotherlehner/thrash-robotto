(ns thrash.map
  "Map generation and interaction functions"
  (:require [thrash.util :as util]
            [thrash.rot :as rot]))

(defn describe-terrain [terrain] (name (:id terrain)))

(def calmwater {:id :calm_water :fg "#7b9ae3" :bg "#3b5dad" :glyph "~"})
(def rapidwater {:id :rapid_water :fg "#7b9ae3" :bg "#3b5dad" :glyph "≈"})
(def tree {:id :tree :fg "#b0c4a5" :bg "#435936" :glyph "^"})
(def dirt {:id :dirt :fg "#c9b3a7" :bg "#69584f" :glyph "."})
(def grass {:id :grass :fg "#5e8249" :bg "#4a6938" :glyph ","})
(def rock {:id :rock :fg "#bfbfbf" :bg "#525252" :glyph "#"})
(def rockrubble {:id :rock_rubble :fg "#9c9c9c" :bg "#424242" :glyph ";"})
(def treerubble {:id :tree_rubble :fg "#a69e9d" :bg "#524342" :glyph "/"})

; Generate a map with terrain! TODO: refactor this to be a callback supplied to rot/map-arena-new
(defn generate-terrain-arena [width height]
  (let [arena (atom (rot/map-arena-new width height))
        noise (rot/simplex-noise-new)]
    (dotimes [i width]
      (dotimes [j height]
        (let [n (js/Math.floor (* 100 (js/Math.abs (.get noise (/ i 25) (/ j 25)))))
              set-map-pos (fn [i j v] (swap! arena conj {(util/encode-coords i j) v}))]
          (cond
            (and (>= n 0) (< n 5)) (set-map-pos i j rapidwater)
            (and (>= n 5) (< n 10)) (set-map-pos i j calmwater)
            (and (>= n 10) (< n 35)) (set-map-pos i j grass)
            (and (>= n 35) (< n 65)) (set-map-pos i j dirt)
            (and (>= n 65) (< n 85)) (set-map-pos i j tree)
            (and (>= n 85) (< n 100)) (set-map-pos i j rock)))))
    (deref arena)))

(defn passable? [map coord]
  (let [t (:id ((util/encode-coords (:x coord) (:y coord)) map))]
    (case t
      :tree false
      :rock false
      :calm_water true
      :rapid_water true
      :dirt true
      :grass true
      :tree_rubble true
      :rock_rubble true
      false)))

(defn random-xy
  ([width height] {:x (Math/floor (* (rot/rng-uniform) width))
                   :y (Math/floor (* (rot/rng-uniform) height))})
  ([map] {:x (Math/floor (* (rot/rng-uniform) (:width map)))
          :y (Math/floor (* (rot/rng-uniform) (:height map)))}))

(defn allowed-spawn-coords? [map x y]
  (let [width (:width map)
        height (:height map)]
    (and
     (< x (dec width))
     (< y (dec height))
     (> x 1)
     (> y 1))))

(defn allowed-player-spawn-coords? [map coord]
  (and
   (passable? map coord)
   (allowed-spawn-coords? map (:x coord) (:y coord))))

(defn allowed-enemy-spawn-coords? [map target distance coord]
  (and
   (passable? map coord)
   (allowed-spawn-coords? map (:x coord) (:y coord))
   (> (util/distance (:x coord) (:y coord) (:x target) (:y target)) distance)))

(defn random-passable-xy [map]
  (loop [i 0]
    (let [coord (random-xy map)]
      (if (allowed-player-spawn-coords? map coord)
        coord
        (recur (inc i))))))

(defn random-distance-away-from [map target distance]
  (loop [i 0]
    (let [coord (random-xy map)]
      (cond
        (> i 100) (do (util/log "ERROR: went through 100 loops") {:x 10 :y 10})
        (allowed-enemy-spawn-coords? map target distance coord) coord
        :else (recur (inc i))))))

(defn at-xy [map x y] ((util/encode-coords x y) map))

(defn at-coords [map coords] (at-xy map (:x coords) (:y coords)))

(defn type-at-coords [map coords] (:id (at-coords map coords)))

(defn destroy-at-coords [map coords]
  (let [encoded-coords (util/encode-coords coords)]
  (case (type-at-coords map coords)
    :rock (assoc map encoded-coords rockrubble)
    :tree (assoc map encoded-coords treerubble)
    map)))