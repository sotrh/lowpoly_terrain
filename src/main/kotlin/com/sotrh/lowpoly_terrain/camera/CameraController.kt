package com.sotrh.lowpoly_terrain.camera

import com.sotrh.lowpoly_terrain.common.Input
import org.joml.Math
import org.joml.Matrix4f
import org.joml.Vector2d
import org.lwjgl.glfw.GLFW

/**
 * Created by benjamin on 10/3/17
 */
class CameraController(val input: Input, val camera: Camera, var baseSpeed: Float = 0.02f) {

    private var cameraUpdated = false
    private val lastCursorPosition = Vector2d()
    private val rotationVector = Vector2d()

    fun processInput() {
        val keyboard = input.keyboard

        if (keyboard.isKeyPressed(GLFW.GLFW_KEY_W)) moveForward()
        if (keyboard.isKeyPressed(GLFW.GLFW_KEY_A)) moveLeft()
        if (keyboard.isKeyPressed(GLFW.GLFW_KEY_S)) moveBackward()
        if (keyboard.isKeyPressed(GLFW.GLFW_KEY_D)) moveRight()

        if (keyboard.isKeyPressed(GLFW.GLFW_KEY_SPACE)) moveUp()
        if (keyboard.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) moveDown()

        val mouse = input.mouse

        if (mouse.cursorInWindow && mouse.isButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT) &&
                lastCursorPosition.x != mouse.xPos && lastCursorPosition.y != mouse.yPos) {
            lastCursorPosition.sub(mouse.xPos, mouse.yPos, rotationVector).mul(baseSpeed.toDouble() * 10)
            camera.yaw += Math.toRadians(rotationVector.x).toFloat()
            camera.pitch += Math.toRadians(rotationVector.y).toFloat()
            cameraUpdated = true
        }

        lastCursorPosition.set(mouse.xPos, mouse.yPos)
    }

    private fun moveDown() {
        cameraUpdated = true
        camera.move(0f, -baseSpeed, 0f)
    }

    private fun moveUp() {
        cameraUpdated = true
        camera.move(0f, baseSpeed, 0f)
    }

    fun processInputThenIfCameraUpdated(block: (CameraController) -> Unit) {
        processInput()
        if (cameraUpdated) block(this)
    }

    private fun moveForward() {
        cameraUpdated = true
        camera.move(0f, 0f, -baseSpeed)
    }

    private fun moveBackward() {
        cameraUpdated = true
        camera.move(0f, 0f, baseSpeed)
    }

    private fun moveLeft() {
        cameraUpdated = true
        camera.move(-baseSpeed, 0f, 0f)
    }

    private fun moveRight() {
        cameraUpdated = true
        camera.move(baseSpeed, 0f, 0f)
    }

    fun getCameraViewMatrix(): Matrix4f {
        cameraUpdated = false
        camera.update()
        return camera.viewMatrix
    }
}