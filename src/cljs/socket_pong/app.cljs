(ns socket-pong.app
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def ^:private PADDLE_WIDTH 5)
(def ^:private PADDLE_HEIGHT 40)
(def ^:private PADDLE_OFFSET 5)

(def ^:private BALL_SIZE 5)

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
  [position]
  (q/fill 255)
  (q/rect PADDLE_OFFSET
          position
          PADDLE_WIDTH
          PADDLE_HEIGHT))

(defn draw-ball
  "Draw ball."
  [{:keys [x dx y dy]}]
  (q/fill 240)
  (q/rect x y
          BALL_SIZE BALL_SIZE))

(defn set-ball-position!
  "Determine the position of the ball."
  [state])

(defn draw-state
  "Draws the current state of the application"
  [state]
  (draw-paddle (:paddle-position state))
  (set-ball-position! state))

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