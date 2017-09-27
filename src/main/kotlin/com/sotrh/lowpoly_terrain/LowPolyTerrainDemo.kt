package com.sotrh.lowpoly_terrain

import com.sotrh.lowpoly_terrain.camera.Camera
import com.sotrh.lowpoly_terrain.common.DEFAULT_FRAGMENT_SHADER
import com.sotrh.lowpoly_terrain.common.DEFAULT_VERTEX_SHADER
import com.sotrh.lowpoly_terrain.common.Window
import com.sotrh.lowpoly_terrain.terrain.Terrain
import com.sotrh.lowpoly_terrain.terrain.TerrainModel
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

object LowPolyTerrainDemo {

    lateinit var terrain: Terrain
    lateinit var terrainModel: TerrainModel

    lateinit var window: Window
    lateinit var camera: Camera

    fun getTime() = GLFW.glfwGetTime()

    fun quit() {
    }

    fun run() {
        setup()
        create()
        loop()
        destroy()
    }

    private fun create() {

    }

    private fun setup() {
        GLFWErrorCallback.createPrint(System.err).set()

        if (!GLFW.glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        window = Window(Window.Hints("Low Poly Terrain", 800, 600))

        GL.createCapabilities()
    }

    private fun loop() {

        GL11.glClearColor(0.4f, 0.4f, 0.5f, 1.0f)

        // create the camera
        camera = Camera(position = Vector3f(0f, 3f, 0f), rotation = Vector3f(45f, 45f, 0f))

        // create the terrain
        terrain = Terrain.Builder.random(100)
        terrainModel = TerrainModel(terrain)

        // bind the terrain vao
        GL30.glBindVertexArray(terrainModel.vao)

        // create the shader
        val vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertexShader, DEFAULT_VERTEX_SHADER)
        GL20.glCompileShader(vertexShader)

        var status = GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS)
        if (status != GL11.GL_TRUE) throw RuntimeException(GL20.glGetShaderInfoLog(vertexShader))

        val fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fragmentShader, DEFAULT_FRAGMENT_SHADER)
        GL20.glCompileShader(fragmentShader)

        status = GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS)
        if (status != GL11.GL_TRUE) throw RuntimeException(GL20.glGetShaderInfoLog(fragmentShader))

        val shaderProgram = GL20.glCreateProgram()
        GL20.glAttachShader(shaderProgram, vertexShader)
        GL20.glAttachShader(shaderProgram, fragmentShader)
        GL30.glBindFragDataLocation(shaderProgram, 0, "fragColor")
        GL20.glLinkProgram(shaderProgram)

        status = GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS)
        if (status != GL11.GL_TRUE) throw RuntimeException(GL20.glGetProgramInfoLog(shaderProgram))

        GL20.glUseProgram(shaderProgram)

        val floatSize = 4

        val posAttrib = GL20.glGetAttribLocation(shaderProgram, "position")
        GL20.glEnableVertexAttribArray(posAttrib)
        GL20.glVertexAttribPointer(posAttrib, 3, GL11.GL_FLOAT, false, 6 * floatSize, 0L)

        val colAttrib = GL20.glGetAttribLocation(shaderProgram, "color")
        GL20.glEnableVertexAttribArray(colAttrib)
        GL20.glVertexAttribPointer(colAttrib, 3, GL11.GL_FLOAT, false, 6 * floatSize, 3L * floatSize)

        val buffer = FloatArray(16)
        val uniModel = GL20.glGetUniformLocation(shaderProgram, "model")
        val model = Matrix4f()
        GL20.glUniformMatrix4fv(uniModel, false, model.get(buffer))

        val uniView = GL20.glGetUniformLocation(shaderProgram, "view")
        val view = camera.getViewMatrix()
        GL20.glUniformMatrix4fv(uniView, false, view.get(buffer))

        val uniProjection = GL20.glGetUniformLocation(shaderProgram, "projection")
        val ratio = 800f / 600f // shouldn't be hard coded
        val projection = Matrix4f().perspective(60f, ratio, 0.1f, 1000f)
        GL20.glUniformMatrix4fv(uniProjection, false, projection.get(buffer))

        // unbind the vao
        GL30.glBindVertexArray(0)

        val secsPerUpdate = 1.0 / 60.0
        var previous = getTime()
        var steps = 0.0

        while (!window.shouldClose) {
            val loopStartTime = getTime()
            val elapsed = loopStartTime - previous
            previous = loopStartTime
            steps += elapsed

            while (steps >= secsPerUpdate) {
                update()
                steps -= secsPerUpdate
            }

            render()

            window.swapBuffers()
            window.pollEvents()
        }
    }

    private fun update() {

    }

    private fun render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        terrainModel.draw()
    }

    private fun destroy() {
        terrainModel.destroy()
        window.destroy()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        LowPolyTerrainDemo.run()
    }
}