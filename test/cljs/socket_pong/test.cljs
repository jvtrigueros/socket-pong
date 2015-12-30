(ns ^:figwheel-load socket-pong.test
  (:require [cljs.test :refer-macros [deftest is run-tests]]
            [socket-pong.app :as app]
            [cljs.pprint :as pp]))

(enable-console-print!)

(deftest test-player-paddle-collision
  (let [paddle-id :player
        state (as-> app/state-schema s
                  (app/set-initial-state s 300 500)
                  (update-in s [:ball] assoc :x (- (+
                                                     (:x (app/paddle s paddle-id))
                                                     (/ (:width (app/paddle s paddle-id)) 2)
                                                     (get-in s [:ball :radius]))
                                                   1)
                             :y (:y (app/paddle s paddle-id))))

        old-dx (get-in state [:ball :dx])
        new-dx (get-in (app/compute-paddle-collision state paddle-id) [:ball :dx])]
    (is (= 0 (+ old-dx new-dx)))))

(deftest test-enemy-paddle-collision
  (let [paddle-id :enemy
        state (as-> app/state-schema s
                  (app/set-initial-state s 300 500)
                  (update-in s [:ball] assoc :x (+ (-
                                                     (:x (app/paddle s paddle-id))
                                                     (/ (:width (app/paddle s paddle-id)) 2)
                                                     (get-in s [:ball :radius]))
                                                   1)
                             :y (:y (app/paddle s paddle-id))))

        old-dx (get-in state [:ball :dx])
        new-dx (get-in (app/compute-paddle-collision state paddle-id) [:ball :dx])]
    (is (= 0 (+ old-dx new-dx)))))
