(ns test-lwjgl.core
  (:require [test-lwjgl.window :as window]
	    [clojure.core.matrix :as m]
            [test-lwjgl.buffers :as buffer]
            [test-lwjgl.transformations :as transformation]
            [clojure.tools.logging :as log])
  (:import (org.lwjgl.glfw GLFW GLFWKeyCallback) 
	   (org.lwjgl BufferUtils)
           (org.lwjgl.system MemoryUtil)
           (org.lwjgl.opengl GL11 GL20 GLCapabilities GL)))

(defn -main [] 
  "Start the game"


  (def window (window/create {:width 1280 :height 960 :title "My Shitty Game"}))


  (def init (window/vertex-setup 
    [{:coordinates [-0.5 -0.5 0.0] :color [1.0 0.0 0.0] :texture [0.0 0.0]}
    {:coordinates [0.5 -0.5 0.0] :color [0.0 1.0 0.0] :texture [1.0 0.0]}
    {:coordinates [-0.5 0.5 0.0] :color [0.0 1.0 0.0] :texture [0.0 1.0]}
    {:coordinates [0.5 0.5 0.0] :color [0.0 0.0 1.0] :texture [1.0 1.0]}]
		
		[3 2 1
		 0 1 2]))

  ;;  Start game loop
  (loop [to-render-functions [init]
         curr (.getTime (new java.util.Date))
         prev (.getTime (new java.util.Date))
         lag (atom 0.0)]

    (swap! lag #(+ % (- curr prev)))
  
    ;;  (handle-inputs)

    ;;  (log/info "previous: " (new java.util.Date prev))
    ;;  (log/info "current: " (new java.util.Date curr))
    ;;  (log/info "elapsed: " (- curr prev))

    (while (>= @lag 0.1)
      ;;  (update)
      (swap! lag #(- % 0.1)))

    ;; (render (/ lag 0.1))
    (window/render window to-render-functions)

    (if (zero? (GLFW/glfwWindowShouldClose window))
      (recur to-render-functions (.getTime (new java.util.Date)) curr lag)))

  (GLFW/glfwDestroyWindow window)
  (GLFW/glfwTerminate))
