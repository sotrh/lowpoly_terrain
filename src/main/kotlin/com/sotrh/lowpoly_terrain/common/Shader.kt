package com.sotrh.lowpoly_terrain.common

import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32

/**
 * Created by benjamin on 9/28/17
 */
class Shader(vsCode: String, fsCode: String, gsCode: String? = null) {
    val id: Int

    private var isBound = false

    private enum class ShaderType { VERTEX, FRAGMENT, GEOMETRY }

    init {
        val vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertexShader, vsCode)
        GL20.glCompileShader(vertexShader)
        assertShaderCompiled(vertexShader, ShaderType.VERTEX)

        val fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fragmentShader, fsCode)
        GL20.glCompileShader(fragmentShader)
        assertShaderCompiled(fragmentShader, ShaderType.FRAGMENT)

        val geometryShader: Int
        if (gsCode != null) {
            geometryShader = GL20.glCreateShader(GL32.GL_GEOMETRY_SHADER)
            GL20.glShaderSource(geometryShader, gsCode)
            GL20.glCompileShader(geometryShader)
            assertShaderCompiled(geometryShader, ShaderType.GEOMETRY)
        } else {
            geometryShader = iNULL
        }

        id = GL20.glCreateProgram()
        GL20.glAttachShader(id, vertexShader)
        GL20.glAttachShader(id, fragmentShader)
        if (geometryShader != iNULL) GL20.glAttachShader(id, geometryShader)
        GL30.glBindFragDataLocation(id, 0, "fragColor") // todo: figure out how to optionally do this
        GL20.glLinkProgram(id)
        assertProgramLinked()
    }

    fun bind() {
        assertNotBound()
        isBound = true
        GL20.glUseProgram(id)
    }

    fun unbind() {
        assertBound()
        isBound = false
        GL20.glUseProgram(0)
    }

    inline fun doBound(block: (Shader)->Unit) {
        bind()
        block(this)
        unbind()
    }

    private val uniforms = mutableMapOf<String, Int>()

    fun getUniformForName(name: String): Int {
        assertBound()
        return uniforms[name] ?: GL20.glGetUniformLocation(id, name).also { uniforms.put(name, it) }
    }

    fun putUniform(name: String, value: Matrix4f, transpose: Boolean = false) {
        putUniform(getUniformForName(name), value, transpose)
    }

    fun putUniform(location: Int, value: Matrix4f, transpose: Boolean = false) {
        assertBound()
        GL20.glUniformMatrix4fv(location, transpose, value.get(MATRIX_BUFFER))
    }

    private fun assertBound() {
        if (!isBound) throw IllegalStateException("You must bind this program ($id) before using it.")
    }

    private fun assertNotBound() {
        if (isBound) throw IllegalStateException("This program ($id) has already been bound.")
    }

    private fun assertProgramLinked() {
        val status = GL20.glGetProgrami(id, GL20.GL_LINK_STATUS)
        if (status != GL11.GL_TRUE)
            throw IllegalStateException("Failed to link program:\n${GL20.glGetProgramInfoLog(id)}")
    }

    private fun assertShaderCompiled(shader: Int, type: ShaderType) {
        val status = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS)
        if (status != GL11.GL_TRUE)
            throw IllegalStateException("Failed to compile ${type.name}:\n${GL20.glGetShaderInfoLog(shader)}")
    }
}