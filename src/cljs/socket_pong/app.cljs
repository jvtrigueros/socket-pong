(ns socket-pong.app
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def ^:private PADDLE_WIDTH 10)
(def ^:private PADDLE_HEIGHT 80)
(def ^:private PADDLE_OFFSET 10)

(def ^:private BALL_SIZE 15)

(def initial-state
  {:winner nil
   :paddle {:x PADDLE_OFFSET,       :dx 0
            :y (/ 300 2),           :dy 0
            :height PADDLE_HEIGHT,  :width PADDLE_WIDTH}
   :ball   {:x (/ 500 2),           :dx -5
            :y (/ 300 2),           :dy -5
            :radius (/ BALL_SIZE 2)}})

(defn setup
  "Setup for Quil"
  []
  (q/color-mode :hsb)
  (q/frame-rate 60)

  initial-state)

(defn draw-paddle
  "Draw paddle"
  [state]
  (let [paddle (:paddle state)
        {:keys [x y height width]} paddle]
    (q/fill 255)
    (q/rect-mode :center)
    (q/rect x y width height)))

(defn draw-ball
  "Draw ball."
  [state]
  (let [ball (:ball state)
        {:keys [x y]} ball]
    (q/fill 240)
    (q/ellipse x y
               BALL_SIZE BALL_SIZE)
    state))

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
  (let [paddle (:paddle state)
        {:keys [y dy]} paddle]
    (assoc-in state [:paddle :y] (+ y dy))))

(defn compute-paddle-collision
  "Change the ball's velocity based on paddle collision"
  [state]
  (let [ball (:ball state)
        paddle (:paddle state)
        {bx :x bdx :dx by :y r :radius} ball
        {px :x py :y h :height w :width} paddle]

    (assoc-in state [:ball :dx]
              (if (and (< (- bx r) (+ px (/ w 2)))                          ; Check for x-collision
                       (< (- py (/ h 2)) (- by r) (+ by r) (+ py (/ h 2)))) ; Check for y-collision
                (* -1 bdx)
                bdx))))

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
  (-> state
      (compute-ball-velocity)
      (compute-ball-position)
      (compute-paddle-position)
      (compute-paddle-collision)))

(defn set-paddle-velocity
  "Set the paddle position based on which key was pressed."
  [state key]
  (let [dy (get-in state [:paddle :dy])]
    (assoc-in state [:paddle :dy] (case key
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
  (assoc-in state [:paddle :dy] 0))

(defn reset
  "Reset state of program."
  [state event]
  initial-state)

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