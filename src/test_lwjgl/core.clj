(ns test-lwjgl.core
  (:use [test-lwjgl.utility])
  (:require [test-lwjgl.window :as window]
						[clojure.java.io :as io]
						[test-lwjgl.parser_3d.ply :as ply]
						[test-lwjgl.shapes.basic :as shape]
            [clojure.core.matrix :as m]
            [clojure.core.matrix.operators :as mo]
            [test-lwjgl.buffers :as buffer]
            [test-lwjgl.state :as state]
            [test-lwjgl.transformations :as transformation]
            [test-lwjgl.camera :as camera]
            [test-lwjgl.shader :as shader]
            [clojure.tools.logging :as log])
  (:import (org.lwjgl.glfw GLFW GLFWKeyCallback)
           (org.lwjgl BufferUtils)
           (org.lwjgl.system MemoryUtil)
           (org.lwjgl.opengl GL11 GL20 GLCapabilities GL))
  (:gen-class))


(defn -main []
  "Start the game"


  (def window (window/create {:width 1280 :height 960 :title "My Shitty Game"}))
 ;; (def ground (window/vertex-setup
 ;;     [{:coordinates [-100.0 0.0 -100.0] :color [0.0 1.0 0.0] :texture [0.0 0.0]}
 ;;      {:coordinates [-100.0 0.0  100.0] :color [0.0 1.0 0.0] :texture [1.0 0.0]}
 ;;      {:coordinates [ 100.0 0.0  100.0] :color [0.0 1.0 0.0] :texture [1.0 1.0]}
 ;;      {:coordinates [ 100.0 0.0  100.0] :color [0.0 1.0 0.0] :texture [0.0 0.0]}
 ;;      {:coordinates [ 100.0 0.0 -100.0] :color [0.0 1.0 0.0] :texture [1.0 0.0]}
 ;;      {:coordinates [-100.0 0.0 -100.0] :color [0.0 1.0 0.0] :texture [1.0 1.0]}
 ;;      ] []))

 (shader/init-defaults)

 (def pandaki (window/vertex-setup (ply/parse-ply "pandaki2.ply") 1))
 (def triangles (window/vertex-setup (shape/triangle true) 200))
 (def cubes (window/vertex-setup (shape/cube true) 100))

(def fps (atom [0 0]))
  ;;  Start game loop
  (loop [to-render-functions [cubes triangles pandaki]
         curr (GLFW/glfwGetTime)
         prev (GLFW/glfwGetTime)
         lag (atom 0.0)]

    (swap! lag #(+ % (- curr prev)))
    (swap! (state/get-atom) assoc :deltatime (- curr prev))

    ;;  (handle-inputs)
    (let [camera (camera/get-atom)
          cam @camera
          front (:front cam)
          up (:up @camera)
          position (:position cam)
          acceleration (:acceleration cam)
          no-change [0.0 0.0 0.0]
          forward (if (acceleration :forward) (mo/* (state/camera-speed) front) no-change)
          backward (if (acceleration :backward) (mo/* -1.0 (state/camera-speed) front) no-change)
          left (if (acceleration :left) (mo/* -1.0 (m/normalise (m/cross front up)) (state/camera-speed)) no-change)
          right (if (acceleration :right) (mo/* (m/normalise (m/cross front up)) (state/camera-speed)) no-change)]
      (swap! camera assoc :position (mo/+ position forward right left backward)))




    ;;  (log/info "previous: " (new java.util.Date prev))
    ;;  (log/info "current: " (new java.util.Date curr))
    ;;  (log/info "elapsed: " (- curr prev))

    (while (>= @lag 0.1)
      ;;  (update)
      (swap! lag #(- % 0.1)))

    ;; (render (/ lag 0.1))
    (window/render window to-render-functions)


    (if (>= (- (GLFW/glfwGetTime) (@fps 1)) 1.0)
  (do (println (str "FPS: " (+ 1 (@fps 0))))
      (swap! fps update-in [1] inc)
      (swap! fps assoc 0 0))
  (swap! fps update-in [0] inc))

    (if (not (GLFW/glfwWindowShouldClose window))
      (recur to-render-functions (GLFW/glfwGetTime) curr lag)))

  (GLFW/glfwDestroyWindow window)
  (GLFW/glfwTerminate))
