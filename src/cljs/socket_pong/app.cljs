(ns socket-pong.app
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def ^:private PADDLE_WIDTH 5)
(def ^:private PADDLE_HEIGHT 40)
(def ^:private PADDLE_OFFSET 5)

(def ^:private BALL_SIZE 10)

(defn setup
  "Setup for Quil"
  []
  (q/color-mode :hsb)
  (q/background 0)

  {:paddle-position (/ (- 300 PADDLE_HEIGHT) 2)
                    :ball {:x (/ 500 2), :dx -1
                           :y (/ 300 2), :dy 0}})

(defn draw-paddle
  "Draw paddle"
  [state]
  (q/fill 255)
  (q/rect PADDLE_OFFSET
          (:paddle-position state)
          PADDLE_WIDTH
          PADDLE_HEIGHT)
  state)

(defn draw-ball
  "Draw ball."
  [state]
  (let [ball (:ball state)
        {:keys [x dx y dy]} ball]
    (q/fill 240)
    (q/rect x y
            BALL_SIZE BALL_SIZE)
    state))

(defn set-ball-position!
  "Determine the position of the ball."
  [state]
  state)

(defn draw-state
  "Draws the current state of the application"
  [state]
  (-> state
      (draw-paddle)
      (set-ball-position!)
      (draw-ball)))

(defn set-paddle-position!
  "Set the paddle position based on which key was pressed."
  [state key]
  (let [old-position (:paddle-position state)
        new-position (case key
                       :up (dec old-position)
                       :down (inc old-position)
                       old-position)]
    (update-in state [:paddle-position] (constantly new-position))))

(defn key-pressed-handler
  "Determine what to do when key is pressed."
  [state event]
  (-> state
      (set-paddle-position! (:key event))))

(q/defsketch socket-pong
             :host "socket-pong"
             :setup setup
             :draw draw-state
             :key-pressed key-pressed-handler
             :middleware [m/fun-mode])

(.log js/console "Socket Pong!")