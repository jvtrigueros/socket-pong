(ns socket-pong.app
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def ^:private PADDLE_WIDTH 5)
(def ^:private PADDLE_HEIGHT 40)
(def ^:private PADDLE_OFFSET 5)

(defn setup
  "Setup for Quil"
  []
  (q/color-mode :hsb)
  (q/background 0)

  {:paddle-position 0
   :ball            {:position  {:x 0 :y 0}
                     :direction {:x 0 :y 0}}})

(defn set-paddle-position!
  "Sets the position of the paddle"
  [position]
  (q/fill 255)
  (q/rect PADDLE_OFFSET
          position
          PADDLE_WIDTH
          PADDLE_HEIGHT))

(defn set-ball-position!
  "Determine the position of the ball."
  [ball]
  )

(defn draw-state
  "Draws the current state of the application"
  [state]
  (set-paddle-position! (:paddle-position state))
  (set-ball-position! (:ball state)))

(defn move
  "Set the paddle position based on which key was pressed."
  [key position]
  (case key
    :up (dec position)
    :down (inc position)
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