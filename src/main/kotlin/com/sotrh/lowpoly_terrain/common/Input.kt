package com.sotrh.lowpoly_terrain.common

import org.lwjgl.glfw.GLFW

class Input(val window: Window) {

    val keyboard = Keyboard()
    val mouse = Mouse()

    fun processInput() {
        keyboard.preProcessInput()
        mouse.preProcessInput()
        GLFW.glfwPollEvents()
    }

    inner class Keyboard {
        private val pressedKeys = mutableSetOf<Int>()
        private val repeatingKeys = mutableSetOf<Int>()
        private val justPressedKeys = mutableSetOf<Int>()

        init {
            window.addKeyEventListener { _, key, _, action, _ ->
                when (action) {
                    GLFW.GLFW_PRESS -> pressedKeys += key
                    GLFW.GLFW_REPEAT -> repeatingKeys += key
                    GLFW.GLFW_RELEASE -> {
                        justPressedKeys += key
                        repeatingKeys -= key
                        pressedKeys -= key
                    }
                }
            }
        }

        fun isKeyPressed(key: Int): Boolean = key in pressedKeys
        fun isKeyRepeating(key: Int): Boolean = key in repeatingKeys
        fun isKeyJustPressed(key: Int): Boolean = key in justPressedKeys

        fun preProcessInput() {
            justPressedKeys.clear()
        }
    }

    inner class Mouse {
        private val pressedButtons = mutableSetOf<Int>()
        private val justPressedButtons = mutableSetOf<Int>()

        var xPos = 0.0; private set
        var yPos = 0.0; private set

        var xScrollOffset = 0.0; private set
        var yScrollOffset = 0.0; private set

        var cursorInWindow = false; private set

        init {
            window.addMouseButtonListener { _, button, action, _ ->
                when(action) {
                    GLFW.GLFW_PRESS -> pressedButtons += button
                    GLFW.GLFW_RELEASE -> {
                        pressedButtons -= button
                        justPressedButtons += button
                    }
                }
            }

            window.addCursorEnteredListener { _, entered ->
                cursorInWindow = entered
            }

            window.addCursorPosListener { _, xpos, ypos ->
                xPos = xpos
                yPos = ypos
            }

            window.addScrollListener { _, xoffset, yoffset ->
                xScrollOffset = xoffset
                yScrollOffset = yoffset
            }
        }

        fun isButtonPressed(button: Int) = button in pressedButtons
        fun isButtonJustPressed(button: Int) = button in justPressedButtons

        fun preProcessInput() {
            xScrollOffset = 0.0
            yScrollOffset = 0.0
            justPressedButtons.clear()
        }
    }
}