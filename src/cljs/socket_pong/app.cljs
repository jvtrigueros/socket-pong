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

(defn paddle [state id] (first (filter #(= id (:id %)) (:paddles state))))

(defn set-initial-state
  "Given a state schema, fill it in with appropriate default values."
  [schema height width]
  (let [paddle-offset 10
        paddle-width 10
        paddle-height 80
        ball-radius 7]
    (-> schema
        (update-in [:paddles 0] assoc :x paddle-offset :y (- (/ height 2) 40) :height paddle-height :width paddle-width)
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

(defn compute-player-position
  [state]
  (let [player (paddle state :player)
        {:keys [y dy]} player]
    ; TODO: I can't do this, cause this wont really work.
    (assoc-in state [:paddles 0 :y] (+ y dy))))

(defn compute-enemy-position
  [state]
  (let [{by :y} (:ball state)]
    (assoc-in state [:paddles 1 :y] by)))

(defn compute-paddle-collision
  "Change the ball's velocity based on paddle collision"
  [state paddle-id]
  (let [ball (:ball state)
        paddle (paddle state paddle-id)
        {bx :x bdx :dx by :y r :radius} ball
        {px :x py :y ph :height pw :width} paddle
        paddle-right-edge (+ px (/ pw 2))
        paddle-left-edge (- px (/ pw 2))
        paddle-top-edge (- py (/ ph 2))
        paddle-bottom-edge (+ py (/ ph 2))
        ball-left-edge (- bx r)
        ball-right-edge (+ bx r)
        ball-top-edge (- by r)
        ball-bottom-edge (+ by r)]

    (.log js/console (str paddle))
    (assoc-in state [:ball :dx]
              (if (and
                    (< paddle-top-edge ball-top-edge ball-bottom-edge paddle-bottom-edge)
                    (or
                      (< paddle-left-edge ball-right-edge (+ paddle-left-edge (Math/abs bdx)))
                      (< (- paddle-right-edge (Math/abs bdx)) ball-left-edge paddle-right-edge)))
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
        (compute-paddle-collision :player)
        (compute-ball-position)
        (compute-player-position)
        (compute-enemy-position)
        (compute-paddle-collision :enemy))
    state))

(defn set-paddle-velocity
  "Set the paddle position based on which key was pressed."
  [state key]
  (let [dy (:dy (paddle state :player))]
    (assoc-in state [:paddles 0 :dy] (case key
                                       :up -8
                                       :down 8
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