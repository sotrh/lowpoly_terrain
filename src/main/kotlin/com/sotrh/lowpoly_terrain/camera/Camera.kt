package com.sotrh.lowpoly_terrain.camera

import org.joml.Matrix4f
import org.joml.Vector3f

/**
 * Created by benjamin on 9/26/17
 */
class Camera(val position: Vector3f = Vector3f(0f, 0f, 0f), val rotation: Vector3f = Vector3f(0f, 0f, 0f)) {

    private val viewMatrix = Matrix4f()

    fun movePosition(offsetX: Float, offsetY: Float, offsetZ: Float) {
        if (offsetZ != 0f) {
            position.x += Math.sin(Math.toRadians(rotation.y.toDouble())).toFloat() * -1.0f * offsetZ
            position.z += Math.cos(Math.toRadians(rotation.y.toDouble())).toFloat() * offsetZ
        }

        if (offsetX != 0f) {
            position.x += Math.sin(Math.toRadians(rotation.y.toDouble())).toFloat() * -1.0f * offsetX
            position.z += Math.cos(Math.toRadians(rotation.y.toDouble())).toFloat() * offsetX
        }

        position.y += offsetY
    }

    fun moveRotation(offsetX: Float, offsetY: Float, offsetZ: Float) {
        rotation.x += offsetX
        rotation.y += offsetY
        rotation.z += offsetZ
    }

    fun getViewMatrix(): Matrix4f {
        return viewMatrix.identity()
                .rotate(Math.toRadians(rotation.x.toDouble()).toFloat(), Vector3f(1f, 0f, 0f))
                .rotate(Math.toRadians(rotation.y.toDouble()).toFloat(), Vector3f(0f, 1f, 0f))
                .translate(position.x, position.y, position.z)
    }
}