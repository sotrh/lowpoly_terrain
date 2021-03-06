package com.sotrh.lowpoly_terrain

import com.sotrh.lowpoly_terrain.camera.CameraController
import com.sotrh.lowpoly_terrain.camera.Camera
import com.sotrh.lowpoly_terrain.common.*
import com.sotrh.lowpoly_terrain.terrain.Terrain
import com.sotrh.lowpoly_terrain.terrain.TerrainModel
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

object LowPolyTerrainDemo {

    lateinit var terrain: Terrain
    lateinit var terrainModel: TerrainModel

    lateinit var window: Window
    lateinit var cameraController: CameraController

    fun getTime() = GLFW.glfwGetTime()

    fun run() {
        setup()
        loop()
        destroy()
    }

    private fun setup() {
        GLFWErrorCallback.createPrint(System.err).set()

        if (!GLFW.glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        window = Window(Window.Hints("Low Poly Terrain", 800, 600))

        // create the camera
        val camera = Camera()
        camera.position.y = 10f
        cameraController = CameraController(window.input, camera)

        // create the terrain
        terrain = Terrain.Builder.random(100)
        terrainModel = TerrainModel(terrain)

        window.addWindowSizeChangedListener {
            terrainModel.shader.bind()
            val ratio = window.width.toFloat() / window.height
            val projection = Matrix4f().setPerspective(60f.toRadians(), ratio, 0.1f, 1000f)
            terrainModel.shader.putUniform("projection", projection)
            terrainModel.shader.unbind()
        }

        GL.createCapabilities()
    }

    private fun loop() {

        GL11.glClearColor(0.4f, 0.4f, 0.5f, 1.0f)
        GL11.glEnable(GL11.GL_DEPTH_TEST)

        // bind the terrainModel
        terrainModel.vao.bind()
        terrainModel.shader.bind()

        val floatSize = 4

        val posAttrib = GL20.glGetAttribLocation(terrainModel.shader.id, "position")
        GL20.glEnableVertexAttribArray(posAttrib)
        GL20.glVertexAttribPointer(posAttrib, 3, GL11.GL_FLOAT, false, 6 * floatSize, 0L)

        val colAttrib = GL20.glGetAttribLocation(terrainModel.shader.id, "color")
        GL20.glEnableVertexAttribArray(colAttrib)
        GL20.glVertexAttribPointer(colAttrib, 3, GL11.GL_FLOAT, false, 6 * floatSize, 3L * floatSize)

        val model = Matrix4f()
        terrainModel.shader.putUniform("model", model)

        terrainModel.shader.putUniform("view", cameraController.getCameraViewMatrix())

        val ratio = window.width.toFloat() / window.height
        val projection = Matrix4f().setPerspective(60f.toRadians(), ratio, 0.1f, 1000f)
        terrainModel.shader.putUniform("projection", projection)

        terrainModel.shader.putUniform("lightDirection", Vector3f(0f, 1f, 0f))
        terrainModel.shader.putUniform("lightColor", Vector3f(1f, 1f, 1f))
        terrainModel.shader.putUniform("lightBias", Vector2f(0.3f, 0.8f))

        terrainModel.shader.unbind()
        terrainModel.vao.unbind()

        val secsPerUpdate = 1.0 / 60.0
        var previous = getTime()
        var steps = 0.0

        while (!window.shouldClose) {
            val loopStartTime = getTime()
            val elapsed = loopStartTime - previous
            previous = loopStartTime
            steps += elapsed

            processInput()

            while (steps >= secsPerUpdate) {
                update()
                steps -= secsPerUpdate
            }

            render()
        }
    }

    private fun processInput() {
        window.input.processInput()

        val keyboard = window.input.keyboard
        if (keyboard.isKeyJustPressed(GLFW.GLFW_KEY_ESCAPE)) {
            window.close()
        }

        cameraController.processInputThenIfCameraUpdated {
            terrainModel.shader.doBound {
                it.putUniform("view", cameraController.getCameraViewMatrix())
            }
        }
    }

    private fun update() {

    }

    private fun render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        terrainModel.draw()

        window.swapBuffers()
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