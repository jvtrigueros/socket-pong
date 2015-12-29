(ns ^:figwheel-load socket-pong.test
  (:require [cljs.test :refer-macros [deftest is run-tests]]
            [socket-pong.app :as app]))

(enable-console-print!)

(deftest test-player-paddle-collision
  (let [paddle-id :player
        state (as-> app/state-schema s
                  (app/set-initial-state s 300 500)
                  (update-in s [:ball] assoc :x (- (+ (:x (app/paddle s paddle-id)) (:width (app/paddle s paddle-id))) 1)
                                             :y (:y (app/paddle s paddle-id))))

        {px :x py :y ph :height pw :width} (app/paddle state paddle-id)
        {bx :x by :y bdx :dx r :radius} (:ball state)

        paddle-right-edge (+ px (/ pw 2))
        paddle-top-edge (- py (/ ph 2))
        paddle-bottom-edge (+ py (/ ph 2))
        ball-left-edge (- bx r)
        ball-top-edge (- by r)
        ball-bottom-edge (+ by r)]
    (is (and
          (< paddle-top-edge ball-top-edge ball-bottom-edge paddle-bottom-edge)
          (< (- paddle-right-edge (Math/abs bdx)) ball-left-edge paddle-right-edge)))))
