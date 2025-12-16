(ns thrash.env
  "Environment state"
  (:require [cljs.core.async :refer [chan close!]]
            [thrash.htm :as htm]
            [thrash.rot :as rot]
            [thrash.entity :as ent]
            [thrash.map :as map]
            [thrash.constants :as const]))

(defn env-new []
  (let [display (rot/reset const/DISPLAY_WIDTH const/DISPLAY_HEIGHT (htm/get-body))
        inputchannel (chan)
        inputlistener (rot/keydown-listener-new inputchannel)
        map (map/generate-terrain-arena const/MAP_WIDTH const/MAP_HEIGHT)
        player (ent/player-new (map/random-passable-xy map))
        opponent (ent/opponent-new (map/random-distance-away-from map player 10))
        scheduler (rot/scheduler-simple-new opponent)
        mobs (zipmap [:player (:id opponent)] [player opponent])]
    {:display display
     :inputchannel inputchannel
     :inputlistener inputlistener
     :scheduler scheduler
     :mobs mobs
     :map map
     :mode :welcome}))

;; Environment (kept in an atom and swapped by sytems that need to change it)
(defonce state (atom (env-new)))

(defn env [key] (get @state key))

(defn get-player [] (:player (env :mobs)))

(defn player-x [] (:x (get-player)))

(defn player-y [] (:y (get-player)))

(defn mob-values [] (vals (env :mobs)))

(defn get-mobs [] (env :mobs))

(defn get-map [] (env :map))

(defn map-keys [] (keys (env :map)))

(defn get-display [] (env :display))

(defn get-scheduler [] (env :scheduler))

(defn get-input-channel [] (env :inputchannel))

(defn update-player [player]
  (swap! state conj {:mobs (assoc (:mobs @state) :player player)})
  (get-player))

(defn update-mob [mob]
  (swap! state conj {:mobs (assoc (:mobs @state) (:id mob) mob)})
  (get-mobs))

(defn remove-mob [mob-id]
  (swap! state conj {:mobs (dissoc (:mobs @state) mob-id)})
  (get-mobs))

(defn add-mob [mob]
  (swap! state conj {:mobs (assoc (:mobs @state) (:id mob) mob)})
  (get-mobs))

(defn mobs-by-component [component]
  (filter (fn [x] (contains? (:components x) component)) (mob-values)))

(defn update-map [new-map]
  (swap! state conj {:map new-map})
  (get-map))

(defn set-mode [m] (swap! state conj {:mode m}))

(defn get-mode [] (env :mode))

(defn reset []
  (rot/keydown-listener-remove (env :inputlistener))
  (close! (env :inputchannel))
  (reset! state (env-new)))

(defn victory! []
  (set-mode :victory)
  (get-mode))

(defn victory? []
  (= (get-mode) :victory))