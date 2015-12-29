(ns ^:figwheel-load socket-pong.test
  (:require [cljs.test :refer-macros [deftest is run-tests]]))

(deftest test-paddle-collision
  (is (= 1 0) "life is good"))

(run-tests)
