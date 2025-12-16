(ns thrash.keys
  "Constants and globals that should only be defined ONCE")

; Control keys
(defonce control
  {76 :look     ; l key, examine a map square
   27 :escape   ; back out of current mode
   82 :reset    ; r key, reset the game
   65 :aim      ; aim or attack
   13 :confirm  ; return key - confirm action
   86 :trigger-victory  ; v key, trigger victory (REMOVE FOR PRODUCTION RELEASE)
   66 :beam       ; b
   71 :guns       ; g
   67 :cannon     ; c
   77 :missiles   ; m
   190 :pass      ; period key (pass a turn)
   })

; Weapon keys
(defonce weapons
  {66 :beam       ; b
   71 :guns       ; g
   67 :cannon     ; c
   77 :missiles   ; m
   })

; key-codes is a map linking the key code with the x/y modification
(defonce direction
  {38 [0 -1] ; up
   39 [1 0]  ; right
   40 [0 1]  ; down
   37 [-1 0] ; left
   })

(defn is-control-key? [keyCode] (contains? control keyCode))

(defn get-control-key [keyCode] (get control keyCode))

(defn is-confirm-key? [keyCode] (= :confirm (get-control-key keyCode)))

(defn is-escape-key? [keyCode] (= :escape (get-control-key keyCode)))

(defn is-look-key? [keyCode] (= :look (get-control-key keyCode)))

(defn is-pass-key? [keyCode] (= :pass (get-control-key keyCode)))

(defn is-direction-key? [keyCode] (contains? direction keyCode))

(defn is-weapon-key? [keyCode] (contains? weapons keyCode))

(defn get-weapon-key [keyCode] (get weapons keyCode))

(defn get-weapon-key-name [weapon]
  (case weapon
    :beam "b"
    :guns "g"
    :cannon "c"
    :missiles "m"))