(ns thrash.draw
  "Drawing functions"
  (:require [thrash.rot :as rot]
            [thrash.entity :as ent]
            [thrash.inspect :as inspect]
            [thrash.map :as map]
            [thrash.util :as util]))

;; Game drawing functions - mostly shorthands or aggregates of function calls

(defn draw-map [display map mobs]
  (rot/draw display map mobs))

;; (defn draw-player-stats [display player]
;;   (rot/draw-str display 0 23 (ent/get-stat-string player)))

; Draw message - line 24 is for general update messages
(defn draw-message [display message]
  (rot/draw-str display 0 24 message))

; During control mode transitions and other things it's necessary to clear the screen for redrawing
(defn draw-clear-screen [display] (rot/draw-clear display))

; Draw normal screen - map with mechs and players and things
(defn draw-normal-screen [display map mobs player]
  (draw-clear-screen display)
  (draw-map display map mobs)
  (rot/draw-str
   display 0 23
   (str (ent/get-stat-string player) " on: " (inspect/description-of-terrain map (:x player) (:y player)))))

; Draw the game welcome splash screen - TODO: currently just draws the initial normal state
;;___                            _                                
;; |   |_   ._   _.   _  |_     |_)   _   |_    _   _|_  _|_   _  
;; |   | |  |   (_|  _>  | |    | \  (_)  |_)  (_)   |_   |_  (_) 
;; Mini by Glenn Chappell 4/93
;; Includes ISO Latin-1
;; figlet release 2.1 -- 12 Aug 1994
;; Permission is hereby given to modify this font, as long as the
;; modifier's name is placed on a comment line.

;; Modified by Paul Burton <solution@earthlink.net> 12/96 to include new parameter
;; supported by FIGlet and FIGWin.  May also be slightly modified for better use
;; of new full-width/kern/smush alternatives, but default output is NOT changed.
(defn draw-welcome-screen [display]
  (draw-clear-screen display)
  (rot/draw-str display 8 10 "___                            _")
  (rot/draw-str display 9 11 "|   |_   ._   _.   _  |_     |_)   _   |_    _   _|_  _|_   _")
  (rot/draw-str display 9 12 "|   | |  |   (_|  _>  | |    | \\  (_)  |_)  (_)   |_   |_  (_)")
  (draw-message display "[Press any key to continue]"))

; Draw the look screen - map alone, no stats, and a cursor
(defn draw-look-screen [display map mobs look-x look-y look-color]
  (draw-clear-screen display)
  (draw-map display map mobs)
  (rot/draw-glyph display look-x look-y (inspect/glyph-at-coords map mobs look-x look-y) "#000" look-color)
  (rot/draw-str display 0 24 (inspect/description-of-coords map mobs look-x look-y)))

; Draw background behind glyph
(defn draw-bg [display map mobs x y color]
    (when (map/at-xy map x y)
      (rot/draw-glyph display x y (inspect/glyph-at-coords map mobs x y) "#000" color)))

; Draw a missile aiming reticle
(defn draw-missile-reticle [display map mobs center-x center-y color]
  (draw-bg display map mobs center-x center-y color)
  (draw-bg display map mobs center-x (dec center-y) color)
  (draw-bg display map mobs center-x (inc center-y) color)
  (draw-bg display map mobs (dec center-x) center-y color)
  (draw-bg display map mobs (inc center-x) center-y color))

; Draws the reticle path a missile will follow in flight
;; (defn draw-missile-reticle-path [display map mobs player-x player-y target-x target-y color]
;;   (let [passable-callback (fn [x y] (boolean (map/at-xy map x y)))
;;         path-to (rot/path-astar-new target-x target-y passable-callback)]
;;     (rot/path-compute
;;      path-to player-x player-y
;;      (fn [path-x path-y]
;;        (draw-bg display map mobs path-x path-y color)))))

; Draw the particle beam reticle
(defn draw-beam-reticle [display map mobs target-x target-y color]
  (draw-bg display map mobs target-x target-y color))

; Draw the cannon reticle
(defn draw-cannon-reticle [display map mobs center-x center-y color]
  (draw-bg display map mobs center-x center-y color))

; Draw the guns reticle
(defn draw-guns-reticle [display map mobs center-x center-y color]
  (draw-bg display map mobs center-x center-y color))

; Draw the aiming screen - map, look descriptive text, cursor, and weapon stats
(defn draw-aiming-screen [display map mobs player look-x look-y look-color weapon]
  (draw-clear-screen display)
  (draw-map display map mobs)
  (case (:id weapon)
    :beam (draw-beam-reticle display map mobs look-x look-y look-color)
    :missiles (draw-missile-reticle display map mobs look-x look-y look-color)
    :cannon (draw-cannon-reticle display map mobs look-x look-y look-color)
    :guns (draw-guns-reticle display map mobs look-x look-y look-color))
  (rot/draw-str display 0 23 (str "press key of weapon to fire: " (ent/get-weapon-stats player)))
  (rot/draw-str display 0 24 (str "target: " (inspect/description-of-coords map mobs look-x look-y))))

; Draw the win screen!
;;    o   _  _|_   _   ._      | 
;;\/  |  (_   |_  (_)  |   \/  o 
;;                         /     
;; Mini by Glenn Chappell 4/93
;; Includes ISO Latin-1
;; figlet release 2.1 -- 12 Aug 1994
;; Permission is hereby given to modify this font, as long as the
;; modifier's name is placed on a comment line.

;; Modified by Paul Burton <solution@earthlink.net> 12/96 to include new parameter
;; supported by FIGlet and FIGWin.  May also be slightly modified for better use
;; of new full-width/kern/smush alternatives, but default output is NOT changed.
(defn draw-win-screen [display]
  (draw-clear-screen display)
  (rot/draw-str display 24 10 "\\  /  o   _  _|_   _   ._      |")
  (rot/draw-str display 25 11 " \\/   |  (_   |_  (_)  |   \\/  o")
  (rot/draw-str display 51 12 "/")
  (draw-message display "[Press any key to reset]"))

(defn draw-beam-effect [sx sy tx ty]
  )