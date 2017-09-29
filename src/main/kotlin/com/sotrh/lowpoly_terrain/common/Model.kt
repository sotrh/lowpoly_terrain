package com.sotrh.lowpoly_terrain.common

import com.sotrh.lowpoly_terrain.camera.Camera

open class Model(val shader: Shader, open val vao: VAO) {
    open fun draw() {
        vao.bind()
        shader.bind()
        vao.draw()
        shader.unbind()
        vao.unbind()
    }
}