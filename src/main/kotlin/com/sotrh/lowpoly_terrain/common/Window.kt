package com.sotrh.lowpoly_terrain.common

import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryStack

class Window(hints: Hints) {

    var id: Long; private set
    var title: String; private set
    var width: Int = 0; private set
    var height: Int = 0; private set

    val shouldClose: Boolean; get() = GLFW.glfwWindowShouldClose(id)

    private val sizeChangeListeners = arrayListOf<(window: Window) -> Unit>()

    init {
        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, hints.resizable)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, hints.contextVersionMajor)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, hints.contextVersionMinor)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, hints.openGLProfile)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)

        title = hints.title
        width = hints.windowWidth
        height = hints.windowHeight

        id = GLFW.glfwCreateWindow(hints.windowWidth, hints.windowHeight, hints.title, lNULL, lNULL)
        if (id == lNULL) throw IllegalStateException("Failed to create GLFW window for $hints")

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            GLFW.glfwGetWindowSize(id, pWidth, pHeight)

            width = pWidth[0]
            height = pHeight[0]

            if (hints.centerInMonitor) centerInMonitor()
        }

        GLFW.glfwSetFramebufferSizeCallback(id) { _, width, height ->
            this.width = width
            this.height = height
            sizeChangeListeners.forEach {
                it(this)
            }
        }

        makeContextCurrent()
        GLFW.glfwSwapInterval(1)
        GLFW.glfwShowWindow(id)
    }

    fun centerInMonitor() {
        assertWindowNotDestroyed()
        val vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
        val xPos = (vidMode.width() - width) / 2
        val yPos = (vidMode.height() - height) / 2
        GLFW.glfwSetWindowPos(id, xPos, yPos)
    }

    fun makeContextCurrent() {
        assertWindowNotDestroyed()
        GLFW.glfwMakeContextCurrent(id)
    }

    fun swapBuffers() {
        assertWindowNotDestroyed()
        GLFW.glfwSwapBuffers(id)
    }

    fun pollEvents() {
        assertWindowNotDestroyed()
        GLFW.glfwPollEvents()
    }

    fun addWindowSizeChangedListener(listener: (window: Window) -> Unit) {
        assertWindowNotDestroyed()
        sizeChangeListeners += listener
    }

    fun destroy() {
        assertWindowNotDestroyed()
        sizeChangeListeners.clear()
        Callbacks.glfwFreeCallbacks(id)
        GLFW.glfwDestroyWindow(id)

        id = lNULL
    }

    private fun assertWindowNotDestroyed() {
        if (id == lNULL) throw IllegalStateException("This window has been destroyed: title = $")
    }

    data class Hints(
            val title: String,
            val windowWidth: Int,
            val windowHeight: Int,
            val centerInMonitor: Boolean = true,
            val resizable: Int = GLFW.GLFW_TRUE,
            val contextVersionMajor: Int = 3,
            val contextVersionMinor: Int = 2,
            val openGLProfile: Int = GLFW.GLFW_OPENGL_CORE_PROFILE
    )
}