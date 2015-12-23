(ns socket-pong.app
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def state-schema
  {:winner  nil
   :paddles [{:id     :player
              :x      0, :dx 0
              :y      0, :dy 0
              :height 0, :width 0}
             {:id     :enemy
              :x      0, :dx 0
              :y      0, :dy 0
              :height 0, :width 0}]
   :ball    {:x      0, :dx -5
             :y      0, :dy -5
             :radius 0}})

(defn player-paddle [state] (get-in state [:paddles 0]))

(defn set-initial-state
  "Given a state schema, fill it in with appropriate default values."
  [schema height width]
  (let [paddle-offset 10
        paddle-width 10
        paddle-height 80
        ball-radius 7]
    (-> schema
        (update-in [:paddles 0] assoc :x paddle-offset :y (/ height 2) :height paddle-height :width paddle-width)
        (update-in [:paddles 1] assoc :x (- width paddle-offset) :y (/ height 2) :height paddle-height :width paddle-width)
        (update-in [:ball] assoc :x (/ width 2) :y (/ height 2) :radius ball-radius))))

(defn setup
  "Setup for Quil"
  []
  (q/color-mode :hsb)
  (q/frame-rate 60)

  (set-initial-state state-schema (q/height) (q/width)))

(defn draw-paddle
  "Draw paddle"
  [state]
  (q/fill 255)
  (q/rect-mode :center)
  (doseq [paddle (:paddles state)
          :let [{:keys [x y height width]} paddle]]
    (q/rect x y width height)))

(defn draw-ball
  "Draw ball."
  [state]
  (let [ball (:ball state)
        {:keys [x y radius]} ball]
    (q/fill 240)
    (q/ellipse-mode :radius)
    (q/ellipse x y radius radius)))

(defn compute-ball-velocity
  "Determine the velocity of the ball."
  [state]
  (let [ball (:ball state)
        {:keys [y dy radius]} ball
        invert-velocity (fn [p v bound]
                          (if (< 0 (- p radius) (+ p radius) bound)
                            v
                            (* -1 v)))
        y-bounds (q/height)]
    (update-in state [:ball]
               assoc
                 :dy (invert-velocity y dy y-bounds))))

(defn compute-ball-position
  "Determine the position of the ball."
  [state]
  (let [ball (:ball state)
        {:keys [x dx y dy]} ball]

    (update-in state [:ball]
               assoc
                :x (+ x dx)
                :y (+ y dy))))

(defn compute-paddle-position
  [state]
  (let [player (player-paddle state)
        {:keys [y dy]} player]
    (assoc-in state [:paddles 0 :y] (+ y dy))))

(defn compute-paddle-collision
  "Change the ball's velocity based on paddle collision"
  [state]
  (let [ball (:ball state)
        paddle (player-paddle state)
        {bx :x bdx :dx by :y r :radius} ball
        {px :x py :y h :height w :width} paddle]

    (assoc-in state [:ball :dx]
              (if (and (< (- (+ px (/ w 2)) (Math/abs bdx)) (- bx r) (+ px (/ w 2))) ; Check for x-collision
                       (< (- py (/ h 2)) (- by r) (+ by r) (+ py (/ h 2))))          ; Check for y-collision
                (* -1 bdx)
                bdx))))

(defn check-winner
  "Determines if there's a winner."
  [state]
  (let [{:keys [x]} (:ball state)]
    (if (< 0 x (q/width))
      state
      (assoc state :winner :paddle))))

(defn draw-state
  "Draws the current state of the application"
  [state]
  (q/background 0)

  (doto state
      (draw-paddle)
      (draw-ball)))

(defn update-state
  "Updates the state of the application"
  [state]
  (if-not (:winner (check-winner state))
    (-> state
        (compute-ball-velocity)
        (compute-ball-position)
        (compute-paddle-position)
        (compute-paddle-collision)
        (check-winner))
    state))

(defn set-paddle-velocity
  "Set the paddle position based on which key was pressed."
  [state key]
  (let [dy (:dy (player-paddle state))]
    (assoc-in state [:paddles 0 :dy] (case key
                                       :up -10
                                       :down 10
                                       dy))))

(defn key-pressed-handler
  "Determine what to do when key is pressed."
  [state event]
  (-> state
      (set-paddle-velocity (:key event))))

(defn key-release-handler
  [state]
  (assoc-in state [:paddles 0 :dy] 0))

(defn reset
  "Reset state of program."
  [_ _]
  (-> state-schema
      (set-initial-state (q/height) (q/width))
      (assoc :winner nil)))

(q/defsketch socket-pong
             :host "socket-pong"
             :setup setup
             :draw draw-state
             :update update-state
             :key-pressed key-pressed-handler
             :key-released key-release-handler
             :key-typed reset
             :middleware [m/fun-mode])

(.log js/console "Socket Pong!")