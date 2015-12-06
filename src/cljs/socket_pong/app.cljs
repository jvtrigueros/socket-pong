(ns socket-pong.app
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(defn setup
  "Setup for Quil"
  []
  (q/color-mode :hsb)
  {:color 0})

(defn draw-state
  "Draws the current state of the application"
  [state]
  (q/background 240)
  (q/fill (:color state) 255 255))

(q/defsketch socket-pong
             :host "socket-pong"
             :setup setup
             :draw draw-state
             :middleware [m/fun-mode])

(.log js/console "Socket Pong!")