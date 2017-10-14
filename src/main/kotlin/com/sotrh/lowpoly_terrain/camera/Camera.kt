package com.sotrh.lowpoly_terrain.camera

import org.joml.Matrix4f
import org.joml.Vector3f

/**
 * Created by benjamin on 10/4/17
 */
class Camera {

    companion object {
        val UP = Vector3f(0f, 1f, 0f).toImmutable()
    }
    val viewMatrix = Matrix4f()

    val position = Vector3f()
    var pitch = 0f
    var yaw = 0f

    private val negativePosition = Vector3f()

    fun move(dx: Float, dy: Float, dz: Float) {
        position.x += Math.sin(yaw.toDouble()).toFloat() * dz
        position.z += Math.cos(yaw.toDouble()).toFloat() * -1f * dz

        position.x += Math.cos(yaw.toDouble()).toFloat() * dx
        position.z += Math.sin(yaw.toDouble()).toFloat() * dx

        position.y += dy
    }

    fun update() {
        viewMatrix.identity()
                .rotate(pitch, 1f, 0f, 0f)
                .rotate(yaw, 0f, 1f, 0f)
                .translate(-position.x, -position.y, -position.z)
    }
}