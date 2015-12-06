(ns socket-pong.app
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def ^:private PADDLE_WIDTH) 5
(def ^:private PADDLE_HEIGHT 20)

(defn setup
  "Setup for Quil"
  []
  (q/color-mode :hsb)
  {
   :paddle-position 0})

(defn draw-state
  "Draws the current state of the application"
  [state]
  (q/background 240)
  (q/rect 0 (:paddle-position state) PADDLE_WIDTH PADDLE_HEIGHT)
  (q/fill (:color state) 255 255))

(defn move
  "Set the paddle position based on which key was pressed."
  [key position]
  (case key
    :up (- position 1)
    :down (+ position 1)
    position))

(defn key-pressed-handler
  "Determine what to do when key is pressed."
  [state event]
  (-> state
      (update-in [:paddle-position] (partial move (:key event)))))

(q/defsketch socket-pong
             :host "socket-pong"
             :setup setup
             :draw draw-state
             :key-pressed key-pressed-handler
             :middleware [m/fun-mode])

(.log js/console "Socket Pong!")