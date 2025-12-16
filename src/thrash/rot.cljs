(ns thrash.rot
  "Convenience functions and wrappers for working with rot.js"
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [thrash.util :as util]
            [cljs.core.async :refer [>!]]))

;; Random Number Generation

(defn rng-uniform [] (. js/ROT.RNG getUniform))

(defn rng-normal 
  ([] (. js/ROT.RNG getNormal))
  ([mean stddev] (. js/ROT.RNG getNormal mean stddev)))

(defn rng-percent [] (. js/ROT.RNG getPercentage))

(defn rng-array-get-item [arr] (. js/ROT.RNG getItem (js-obj arr)))

;; Scheduling

(defn scheduler-add [scheduler actor]
  (. scheduler add actor))

(defn scheduler-next [scheduler]
  (. scheduler next))

(defn scheduler-simple-new 
  ([] (js/ROT.Scheduler.Simple.))
  ([& actors] (let [scheduler (js/ROT.Scheduler.Simple.)]
                (map (partial scheduler-add scheduler) actors) 
                scheduler)))

;; Maps

(defn map-digger-new [width height]
  (js/ROT.Map.Digger. #js {:width width :height height}))

(defn map-digger [width height callback]
  (.create (map-digger-new width height) callback))

(defn map-arena-new [width height]
  (let [map (atom {:width width :height height})]
    (.create (js/ROT.Map.Arena. width height) 
             (fn [x y _] 
               (swap! map conj {(keyword (str x "_" y)) "."})))
    (deref map)))

;; Noise

(defn simplex-noise-new [] (js/ROT.Noise.Simplex.))

(defn simplex-noise-get [noise i j] (.get noise i j))

;; Pathfinding

(defn path-dij-new [target-x target-y passable-callback] 
  (js/ROT.Path.Dijkstra. target-x target-y passable-callback))

(defn path-astar-new [target-x target-y passable-callback]
  (js/ROT.Path.AStar. target-x target-y passable-callback))

(defn path-compute [path source-x source-y path-callback]
  (.compute path source-x source-y path-callback))

;; Convenience functions for the game

(defn keydown-listener-new [channel]
  (js/document.addEventListener 
   "keydown" 
   (fn [e] (go (>! channel e)))))

(defn keydown-listener-remove [listener]
  (js/document.removeEventListener "keydown" listener))

(defn display-new [width height fontSize fontFamily fg bg spacing layout]
  (js/ROT.Display. #js {:width width
                        :height height
                        :fontSize fontSize
                        :fontFamily fontFamily
                        :fg fg
                        :bg bg
                        :spacing spacing
                        :layout layout}))

(defn display-attach [display element]
  (.appendChild element (.getContainer display)))

(defn reset [width height element]
  (set! (.-innerHTML element) "")
  (let [display (display-new 
                 width 
                 height
                 18
                 "Courier New"
                 "#bbb"
                 "#000"
                 1
                 "rect")]
    (display-attach display element)
    display))

(defn draw-clear [display]
  (.clear display))

(defn draw-entity [display e]
  (.draw display (:x e) (:y e) (:glyph e) (:fg e) (:bg e)))

(defn draw-glyph
  ([display x y glyph] (.draw display x y glyph))
  ([display x y glyph foreground] (.draw display x y glyph foreground))
  ([display x y glyph foreground background] (.draw display x y glyph foreground background)))

(defn draw-str [display x y str]
  (.drawText display x y str))

(defn draw-map [display map]
  (doseq [[k v] (seq map)]
    (let [position (util/decode-coords k)]
      (draw-glyph display (first position) (second position) (:glyph v) (:fg v) (:bg v)))))

(defn draw-mobs [display mobs]
  (doseq [m mobs]
    (draw-entity display m)))

(defn draw [display map mobs]
  (draw-map display map)
  (draw-mobs display mobs))