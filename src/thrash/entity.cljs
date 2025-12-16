(ns thrash.entity
  "Functions for working with entities"
  (:require [thrash.util :as util]
            [thrash.keys :as keys]
            [clojure.string :as str]))

; Used to keep track of ID numbers we have used and associated with entities and things
(defonce idcounter (atom 0))
(defn next-id [] (swap! idcounter inc))

(defn player-new [xycoord]
  {:id (next-id) :name "Unnamed" :x (:x xycoord) :y (:y xycoord) :glyph "@" :fg "#fff" :bg "#000"
   :ph 100 :mh 600 :atk 10 :def 10
   :components #{:player} :weapons #{:beam :guns :cannon :missiles}})

(defn opponent-new [xycoord]
  {:id (next-id) :name "Unnamed" :x (:x xycoord) :y (:y xycoord) :glyph "M" :fg "#fc8e44" :bg "#000"
   :ph 100 :mh 600 :atk 10 :def 10
   :components #{:hostile} :weapons #{:beam :guns :cannon :missiles}})

(defn missile-new [xycoord target-coords]
  {:id (next-id) :name "missile" :x (:x xycoord) :y (:y xycoord) :glyph "*" :fg "#fff" :bg "#000"
   :target-coords target-coords :components #{:missile}})

(defn has-component? [e component]
  (contains? (:components e) component))

(defn has-weapon? [e weapon]
  (contains? (:weapons e) weapon))

(defn get-weapon [weapon]
  (case weapon
    :beam {:id :beam :max_range 8 :damage 150 :aoe :line}
    :guns {:id :guns :max_range 12 :damage 50 :aoe :scatter}
    :cannon {:id :cannon :max_range 30 :damage 250 :aoe :circle :aoe_size 1}
    :missiles {:id :missiles :max_range 15 :damage 80 :aoe :circle :aoe_size 1}))

; TODO: remove this function in favor of a more generic one
(defn get-weapon-max-range [weapon]
  (case weapon
    :beam (:max_range (get-weapon :beam))
    :guns (:max_range (get-weapon :guns))
    :cannon (:max_range (get-weapon :cannon))
    :missiles (:max_range (get-weapon :missiles))))

; TODO: remove this function in favor of a more generic one
(defn get-weapon-damage [weapon]
  (case weapon
    :beam (:damage (get-weapon :beam))
    :guns (:damage (get-weapon :guns))
    :cannon (:damage (get-weapon :cannon))
    :missiles (:damage (get-weapon :missiles))))

(defn in-range-for-weapon? [weapon source-coord target-coord]
  (let [max-range (get-weapon-max-range weapon)
        calculated-distance (util/distance (:x source-coord) (:y source-coord) (:x target-coord) (:y target-coord))]
    (util/log "weapon in range?" (str weapon)
              "range:" (str max-range)
              "source:" (str source-coord)
              "target:" (str target-coord)
              "distance:" (str calculated-distance))
    (<= calculated-distance max-range)))

(defn get-weapon-stats [e]
  (zipmap (:weapons e) (map keys/get-weapon-key-name (:weapons e))))

(defn get-stat-string [e]
  (str "PH: " (:ph e) " MH: " (:mh e)))

(defn describe-component [c]
  (str c))

(defn describe-entity [e]
  (str "id: " (:id e)
       ", mech integrity: " (:mh e)
       ", components: " (str/join ", " (map describe-component (:components e)))))

(defn get-coords [e] {:x (:x e) :y (:y e)})

(defn act [e map]
  (util/log "ACT:" (str e)))

(defn is-at-coords? [x y ent]
  (and (= (:x ent) x) (= (:y ent) y)))

(defn mob-at-xy? [mobs x y]
  (boolean (not-empty (filter (partial is-at-coords? x y) mobs))))

(defn first-mob-at-xy [mobs x y]
  (first (filter (partial util/is-at-coords? x y) mobs)))

(defn first-mob-at-coords [mobs coords]
  (first-mob-at-xy mobs (:x coords) (:y coords)))

(defn first-damageable-mob-at-coords [mobs coords]
  (first (filter (fn [x]
                   (and
                    (util/is-at-coords? (:x coords) (:y coords) x)
                    (has-component? x :damageable)))
                 mobs)))