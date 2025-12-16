(ns thrash.interact
  "High level functions for game interactivity"
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [thrash.util :as util]
   [thrash.keys :as keys]
   [thrash.inspect :as inspect]
   [thrash.entity :as ent]
   [thrash.env :as env]
   [thrash.map :as map]
   [clojure.string :as str]
   [thrash.rot :as rot]
   [cljs.core.async :refer [<! chan close! timeout]]
   [thrash.draw :as draw]))

(defn player-process-key [player keycode map mobs]
  (let [pos-change (get keys/direction keycode)]
    (if
     (and
      (not (nil? pos-change))
      (inspect/move-allowed? map mobs (+ (:x player) (first pos-change)) (+ (:y player) (second pos-change))))
      (assoc player
             :x (+ (:x player) (first pos-change))
             :y (+ (:y player) (second pos-change)))
      player)))

(defn player-process-input [player map keyCode mobs]
  (if (keys/is-pass-key? keyCode)
    player
    (player-process-key player keyCode map mobs)))

(defn get-updated-position [map movable pos-change]
  (let [newx (+ (:x movable) (first pos-change))
        newy (+ (:y movable) (second pos-change))]
    (if
     (and
      (>= newx 0) ; TODO: move this logic to map.cljs where it belongs
      (>= newy 0)
      (< newx (:width map))
      (< newy (:height map)))
      (assoc movable :x newx :y newy)
      movable)))

(defn direct-damage-mob [mob weapon]
  (util/log "direct-damage-mob" (str mob) (str weapon))
  (let [damage (ent/get-weapon-damage weapon)
        new-mh (- (:mh mob) damage)]
    (if (<= new-mh 0)
      (do
        (env/remove-mob (:id mob))
        (env/victory!)
        (str "Thrashed " (:name mob)))
      (do
        (env/update-mob (assoc mob :mh new-mh))
        (str damage " damage to " (:name mob))))))

(defn direct-damage-map [target-coords]
  (env/update-map (map/destroy-at-coords (env/get-map) target-coords))
  (str ""))

(defn direct-damage [target-coords weapon]
  (util/log "direct-damage" (str target-coords) (str weapon))
  (let [mob (ent/first-damageable-mob-at-coords (env/mob-values) target-coords)]
    (if (not (nil? mob))
      (direct-damage-mob mob weapon)
      (direct-damage-map target-coords))))

(defn missile-damage [target-coords]
  (let [x (:x target-coords) y (:y target-coords)]
    ; damage every affected square as a missile hit
    (str/join
     ","
     (list
      (direct-damage {:x x :y y} :missiles)
      (direct-damage {:x x :y (dec y)} :missiles)
      (direct-damage {:x (dec x) :y y} :missiles)
      (direct-damage {:x x :y (inc y)} :missiles)
      (direct-damage {:x (inc x) :y y} :missiles)))))

; damages a path from the source to the target
(defn beam-damage [source-coords target-coords]
  (let [ch (chan)
        tx (:x target-coords)
        ty (:y target-coords)
        x (atom (:x source-coords))
        y (atom (:y source-coords))]
    (go
      (loop []
        (let [dirx (Math/sign (- tx @x))
              diry (Math/sign (- ty @y))]
          (swap! x (partial + dirx))
          (swap! y (partial + diry))
          (rot/draw-glyph (env/get-display) @x @y " " "" "#f200ff"))
        (when (not (util/xy-equal? @x @y tx ty))
          (<! (timeout 1000))
          (recur))))
    (close! ch)))

; Fire weapon - player weapon firing action, no need to consider enemy stuff in here
(defn fire-weapon [player weapon target-coords]
  (let [source-x (:x player)
        source-y (:y player)]
    (if (util/coords-equal? player target-coords)
      "You can't shoot yourself"
      (if (ent/has-weapon? player weapon)
        (let [player-coords (ent/get-coords player)]
          (if (ent/in-range-for-weapon? weapon player-coords target-coords)
            (case weapon
              :missiles (do
                          (env/add-mob (ent/missile-new {:x source-x :y source-y} target-coords))
                          "Launched missile!")
              :beam (do
                      (beam-damage (ent/get-coords player) target-coords)
                      "Firing particle beam")
              (direct-damage target-coords weapon))
            "Out of range for that weapon"))
        "You don't have that weapon"))))