(ns socket-pong.app
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def ^:private PADDLE_WIDTH 10)
(def ^:private PADDLE_HEIGHT 80)
(def ^:private PADDLE_OFFSET 10)

(def ^:private BALL_SIZE 15)

(def initial-state
  {:paddle {:x PADDLE_OFFSET,       :dx 0
            :y (/ 300 2),           :dy 0
            :height PADDLE_HEIGHT,  :width PADDLE_WIDTH}
   :ball   {:x (/ 500 2),           :dx -1
            :y (/ 300 2),           :dy -1}})

(defn setup
  "Setup for Quil"
  []
  (q/color-mode :hsb)
  (q/frame-rate 60)

  initial-state)

(defn draw-paddle
  "Draw paddle"
  [state]
  (q/fill 255)
  (q/rect-mode :center)
  (q/rect PADDLE_OFFSET
          (:paddle state)
          PADDLE_WIDTH
          PADDLE_HEIGHT)
  state)

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
        {:keys [x dx y dy]} ball
        radius (/ BALL_SIZE 2)
        invert-velocity (fn [p v bound]
                          (if (< 0 (- p radius) (+ p radius) bound)
                            v
                            (* -1 v)))
        x-bounds 500
        y-bounds 300]
    (update-in state [:ball]
               assoc
                 :dx (invert-velocity x dx x-bounds)
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

(defn draw-state
  "Draws the current state of the application"
  [state]
  (q/background 0)

  (-> state
      (draw-paddle)
      (draw-ball)))

(defn update-state
  "Updates the state of the application"
  [state]
  (-> state
      (compute-ball-velocity)
      (compute-ball-position)))

(defn set-paddle-position!
  "Set the paddle position based on which key was pressed."
  [state key]
  (let [old-position (:paddle state)
        new-position (case key
                       :up (dec old-position)
                       :down (inc old-position)
                       old-position)]
    (update-in state [:paddle] (constantly new-position))))

(defn key-pressed-handler
  "Determine what to do when key is pressed."
  [state event]
  (-> state
      (set-paddle-position! (:key event))))

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
             :key-typed reset
             :middleware [m/fun-mode])

(.log js/console "Socket Pong!")