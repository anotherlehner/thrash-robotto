(ns thrash.game
  "Core game module and run loop"
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [thrash.keys :as keys]
            [thrash.entity :as ent]
            [thrash.util :as util]
            [thrash.interact :as interact]
            [thrash.draw :as draw]
            [thrash.env :as env]))

;; Look state - why store this here? maybe it should be function local?
(defonce look-color "#ff9d96")
(defonce look-coords (atom {:x 0 :y 0}))
(defn get-look-coords [] @look-coords)
(defn look-x [] (:x @look-coords))
(defn look-y [] (:y @look-coords))

; Aiming state
(defonce aim-color "#ff9d96")
(defonce aim-state (atom {:x 0 :y 0 :weapon nil}))
(defn get-aim-coords [] @aim-state)
(defn aim-x [] (:x @aim-state))
(defn aim-y [] (:y @aim-state))
(defn aim-weapon [] (:weapon @aim-state))

(defn missile-fly [missile]
  (let [target-coords (:target-coords missile)
        tx (:x target-coords)
        ty (:y target-coords)
        mx (:x missile)
        my (:y missile)
        dirx (Math/sign (- tx mx)) ; add speed by multiplying dirx by speed? without going over?
        diry (Math/sign (- ty my))]
    (assoc missile :x (+ mx dirx) :y (+ my diry))))

; System for handling all entities with the :missile component
(defn missile-system [missiles]
  (doseq [m missiles]
    ; is m at destination? if so - blow up
    ; else advance m toward destination along path
    (let [target-coords (:target-coords m)]
      (if (util/coords-equal? (ent/get-coords m) (:target-coords m))
        (do 
          (interact/missile-damage target-coords)
          (env/remove-mob (:id m)))
        (env/update-mob (missile-fly m))))))

; Normal mode
; each time through normal mode another entity is allowed to act
(defn do-normal-mode [keyCode]
  ; player key input processing (ie player turn)
  (env/update-player (interact/player-process-input (env/get-player) (env/get-map) keyCode (env/mob-values)))
  ; opponent turn (this would need to be a loop if there were multiple actors!)
  ;; (ent/act (rot/scheduler-next (env/get-scheduler)) (env/get-map))
  ; execute missile system on all missiles-in-flight
  (missile-system (env/mobs-by-component :missile))
  ; draw the normal screen update after actions processed
  (draw/draw-normal-screen (env/get-display) (env/get-map) (env/mob-values) (env/get-player)))

; Look
(defn do-look-mode [keyCode]
  (let [new-position (interact/get-updated-position (env/get-map) (get-look-coords) (get keys/direction keyCode))]
    (swap! look-coords conj new-position)
    (draw/draw-look-screen (env/get-display) (env/get-map) (env/mob-values) (look-x) (look-y) look-color)))

; Aim
(defn do-aiming-mode [keyCode]
  (cond
    (keys/is-direction-key? keyCode)
    (let [weapon (ent/get-weapon (aim-weapon))
          player (env/get-player)
          new-position (interact/get-updated-position (env/get-map)
                                                      (get-aim-coords)
                                                      (get keys/direction keyCode))]
      (when (<= (util/distance (:x player) (:y player) (:x new-position) (:y new-position)) (:max_range weapon))
        (swap! aim-state conj new-position))
      (draw/draw-aiming-screen (env/get-display) (env/get-map) (env/mob-values) (env/get-player) (aim-x) (aim-y) aim-color weapon))
    (keys/is-confirm-key? keyCode)
    (let [resultmsg (interact/fire-weapon (env/get-player) (aim-weapon) (get-aim-coords))]
      ; if the player hasn't won then we should automatically return to normal mode for the next turn
      (when (not (env/victory?)) (env/set-mode :normal))
      (draw/draw-clear-screen (env/get-display))
      (draw/draw-normal-screen (env/get-display) (env/get-map) (env/mob-values) (env/get-player))
      (draw/draw-message (env/get-display) resultmsg))))

; Transition to normal mode
(defn enter-normal-mode []
  (env/set-mode :normal)
  (draw/draw-clear-screen (env/get-display))
  (draw/draw-normal-screen (env/get-display) (env/get-map) (env/mob-values) (env/get-player))
  (util/log "entered normal mode"))

; Transition to aiming mode for a particular weapon
(defn enter-aim-mode [weapon]
  (swap! aim-state conj {:x (env/player-x) :y (env/player-y) :weapon weapon})
  (env/set-mode :aim)
  (draw/draw-clear-screen (env/get-display))
  (draw/draw-aiming-screen (env/get-display) (env/get-map) (env/mob-values) (env/get-player) (aim-x) (aim-y) aim-color (ent/get-weapon (aim-weapon)))
  (util/log "enter aiming mode"))

; Transition to look/examine mode
(defn enter-look-mode []
  (swap! look-coords conj {:x (env/player-x) :y (env/player-y)})
  (env/set-mode :look)
  (draw/draw-clear-screen (env/get-display))
  (draw/draw-look-screen (env/get-display) (env/get-map) (env/mob-values) (look-x) (look-y) look-color)
  (util/log "enter look mode"))

; Reset the game and create a new map
(defn reset-game []
  (env/reset)
  (enter-normal-mode)
  ;; (draw/draw-clear-screen (env/get-display))
  ;; (draw/draw-welcome-screen (env/get-display))
  ;; (util/log "reset game, mode is now" (name (env/get-mode)))
  )

; Game loop
; key presses drive the entire system - each key press change the state
; pressing control keys changes the processing function in the main game loop eg normal -> look
; pressing escape always exits a control mode and returns to normal
; NOTE: entities should only act during NORMAL MODE, not within this loop
(defn run-game []
  (reset-game)
  (go
    (loop [n 0]
      (let [inputevent (<! (env/get-input-channel))
            keyCode (.-keyCode inputevent)]
        ;; (js/console.log keyCode)
        (when (= (env/get-mode) :welcome)
          (enter-normal-mode)
          (recur (inc n)))
        (when (env/victory?)
          (reset-game)
          (enter-normal-mode)
          (recur (inc n)))
        (case (keys/get-control-key keyCode)
          :look (enter-look-mode)
          :escape (enter-normal-mode)
          :trigger-victory (env/victory!)
          :reset (reset-game)
          (:beam :guns :cannon :missiles) (enter-aim-mode (keys/get-control-key keyCode))
          (case (env/get-mode)
            :normal (do-normal-mode keyCode)
            :look (do-look-mode keyCode)
            :aim (do-aiming-mode keyCode)
            nil)))
      (when (env/victory?)
        (draw/draw-win-screen (env/get-display)))
      (recur (inc n)))))
