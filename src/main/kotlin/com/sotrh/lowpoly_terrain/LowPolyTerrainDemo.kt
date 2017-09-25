package com.sotrh.lowpoly_terrain

import com.sotrh.lowpoly_terrain.common.DEFAULT_FRAGMENT_SHADER
import com.sotrh.lowpoly_terrain.common.DEFAULT_VERTEX_SHADER
import com.sotrh.lowpoly_terrain.common.lNULL
import com.sotrh.lowpoly_terrain.common.use
import com.sotrh.lowpoly_terrain.rendering.VAO
import org.joml.Matrix4f
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryStack

object LowPolyTerrainDemo {
    private var window = lNULL

    var windowWidth = 0; private set
    var windowHeight = 0; private set

    fun getTime() = GLFW.glfwGetTime()

    fun quit() {
        GLFW.glfwSetWindowShouldClose(window, true)
    }

    fun run() {
        create()
        loop()
        destroy()
    }

    private fun create() {
        GLFWErrorCallback.createPrint(System.err).set()

        if (!GLFW.glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)

        window = GLFW.glfwCreateWindow(800, 600, "Hello GLFW!", lNULL, lNULL)
        if (window == lNULL) throw RuntimeException("Failed to create the GLFW window")

        GLFW.glfwSetKeyCallback(window) { _, key, scancode, action, mods ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
                quit()
        }

        GLFW.glfwSetFramebufferSizeCallback(window) { _, width, height ->
            windowWidth = width
            windowHeight = height
        }

        // get the thread stack and push a new frame
        MemoryStack.stackPush().also { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            GLFW.glfwGetWindowSize(window, pWidth, pHeight)

            val vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

            val xPos = (vidMode.width() - pWidth.get(0)) / 2
            val yPos = (vidMode.height() - pHeight.get(0)) / 2
            GLFW.glfwSetWindowPos(window, xPos, yPos)

        }.pop()

        GLFW.glfwMakeContextCurrent(window)
        GLFW.glfwSwapInterval(1)
        GLFW.glfwShowWindow(window)
    }

    private lateinit var vao: VAO

    private fun loop() {
        GL.createCapabilities()

        GL11.glClearColor(0.4f, 0.4f, 0.5f, 1.0f)

        vao = VAO.Builder()
                .triangles()
                .vertices(3, 6, true) { vertices ->
                    vertices.put(-0.6f).put(-0.4f).put(0f).put(1f).put(0f).put(0f)
                    vertices.put(0.6f).put(-0.4f).put(0f).put(0f).put(1f).put(0f)
                    vertices.put(0.0f).put(0.6f).put(0f).put(0f).put(0f).put(1f)
                    vertices.flip()
                }
                .build()
        vao.bind()

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
        val view = Matrix4f()
        GL20.glUniformMatrix4fv(uniView, false, view.get(buffer))

        val uniProjection = GL20.glGetUniformLocation(shaderProgram, "projection")
        val ratio = 800f / 600f // shouldn't be hard coded
        println("ratio = $ratio, windowWidth = $windowWidth, windowHeight = $windowHeight")
        val projection = Matrix4f().ortho(-ratio, ratio, -1f, 1f, -1f, 1f)
        GL20.glUniformMatrix4fv(uniProjection, false, projection.get(buffer))

        vao.unbind()

        var secsPerUpdate = 1.0 / 60.0
        var previous = getTime()
        var steps = 0.0

        while (!GLFW.glfwWindowShouldClose(window)) {
            var loopStartTime = getTime()
            var elapsed = loopStartTime - previous
            previous = loopStartTime
            steps += elapsed

            while (steps >= secsPerUpdate) {
                update()
                steps -= secsPerUpdate
            }

            render()

            GLFW.glfwSwapBuffers(window)
            GLFW.glfwPollEvents()
        }
    }

    private fun update() {

    }

    private fun render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)

        vao.bind()
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
        vao.unbind()
    }

    private fun destroy() {
        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        LowPolyTerrainDemo.run()
    }
}