(ns test-lwjgl.config.controls
  (:require [clojure.tools.logging :as log]
	          [clojure.core.matrix :as m]
	          [clojure.core.matrix.operators :as mo]
	          [test-lwjgl.state :as state]
            [test-lwjgl.camera :as camera])
  (:import (org.lwjgl.glfw GLFW GLFWKeyCallback GLFWCursorPosCallback)
           (org.lwjgl.opengl GL11)))

(defn default-release [win]
  (log/info "key released")
)

(defn default-press [win]
  (log/info "key pressed")
)

(defn default-repeat [win]
  (log/info "key repeated")
)

(defn close-window [win]
  (GLFW/glfwSetWindowShouldClose win true)
)

(defn enable-acceleration [direction]
  (fn [window] (swap! (camera/get-atom) assoc-in [:acceleration direction] true)))

(defn disable-acceleration [direction]
  (fn [window] (swap! (camera/get-atom) assoc-in [:acceleration direction] false)))

(def key-bindings
  "Bind key codes with a vector of three actions indexed as default key events:
  press, released, repeat"
  ;;  key-bindings is a map of key codes associated with a 3 element vector.
  ;;  whose indexes represent the key actions:
  ;;
  ;;  key_code: integer     0: GLWF_RELEASE  1: GLWF_PRESS  2: GLFW_REPEAT
  {
      GLFW/GLFW_KEY_ESCAPE [default-release close-window  default-repeat]
      GLFW/GLFW_KEY_ENTER  [default-release default-press default-repeat]
      GLFW/GLFW_KEY_W  [(disable-acceleration :forward) (enable-acceleration :forward) default-repeat]
      GLFW/GLFW_KEY_S  [(disable-acceleration :backward) (enable-acceleration :backward) default-repeat]
      GLFW/GLFW_KEY_A  [(disable-acceleration :left) (enable-acceleration :left) default-repeat]
      GLFW/GLFW_KEY_D  [(disable-acceleration :right) (enable-acceleration :right) default-repeat]
  }
)

(def key-callback (proxy [GLFWKeyCallback] []
  (invoke [win key scancode action mods]
    (let [key-actions (key-bindings key)]
      (if (not (nil? key-actions))
        ((key-actions action) win))))))

(def mouse-callback (proxy [GLFWCursorPosCallback] []
  (invoke [win xpos ypos]
    (let [sensivity (state/get-data :mouse-sensivity)
          last-x (state/mouse-position :x)
          last-y (state/mouse-position :y)
          x-offset (* sensivity (- xpos last-x))
          y-offset (* sensivity (- last-y ypos))
          yaw (+ x-offset (state/mouse-position :yaw))
          pitch (+ y-offset (state/mouse-position :pitch))
          pitch (if (< pitch -89.0) -89.0 pitch)
          pitch (if (> pitch 89.0) 89.0 pitch)
          pitch-cos (Math/cos (Math/toRadians pitch))
          front-x (* (Math/cos (Math/toRadians yaw)) pitch-cos)
          front-y (Math/sin (Math/toRadians pitch))
          front-z (* (Math/sin (Math/toRadians yaw)) pitch-cos)
          front (m/normalise [front-x front-y front-z])]
      (swap! (state/get-atom) assoc-in [:mouse-position] {:x xpos :y ypos :yaw yaw :pitch pitch})
      (swap! (camera/get-atom) assoc-in [:front] front)))))
