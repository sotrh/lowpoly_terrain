package com.sotrh.lowpoly_terrain.rendering

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import java.nio.FloatBuffer

class VAO(val id: Int, val mode: Int, val vbo: Int, val vertexCount: Int, val ebo: Int, val elementCount: Int) {

    private var isDeleted = false
    private var isBound = false

    fun bind() {
        assertProperState()
        GL30.glBindVertexArray(id)
    }

    fun unbind() {
        assertProperState()
        GL30.glBindVertexArray(id)
    }

    fun delete() {
        assertProperState()

        bind()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        if (vbo != 0) {
            GL15.glDeleteBuffers(vbo)
        }
        if (ebo != 0) {
            GL15.glDeleteBuffers(ebo)
        }
        unbind()
        GL30.glDeleteVertexArrays(id)

        isDeleted = true
    }

    private fun assertProperState() {
        if (isDeleted) throw IllegalStateException("This VAO has been deleted")
        else if (isBound) throw IllegalStateException("This VAO has already been bound")
    }

    class Builder {
        private val vao = GL30.glGenVertexArrays()
        private var mode = GL11.GL_TRIANGLES
        private var vbo = 0
        private var vertexCount = 0
        private var ebo = 0
        private var elementCount = 0

        fun points(): VBOStep {
            mode = GL11.GL_POINTS
            return VBOStep()
        }

        fun lines(): VBOStep {
            mode = GL11.GL_LINES
            return VBOStep()
        }

        fun triangles(): VBOStep {
            mode = GL11.GL_TRIANGLES
            return VBOStep()
        }

        fun triangleFan(): VBOStep {
            mode = GL11.GL_TRIANGLE_FAN
            return VBOStep()
        }

        fun triangleStrip(): VBOStep {
            mode = GL11.GL_TRIANGLE_STRIP
            return VBOStep()
        }

         inner class VBOStep {
            fun vertices(vertexCount: Int, buffer: FloatBuffer, isStatic: Boolean = true): EBOStep {
                setVertices(this@Builder, vertexCount, buffer, isStatic)
                return EBOStep()
            }

            fun vertices(vertexCount: Int, floatsPerVertex: Int, isStatic: Boolean, block: (FloatBuffer) -> Unit): EBOStep {
                MemoryStack.stackPush().use { stack ->
                    val buffer = stack.mallocFloat(vertexCount * floatsPerVertex)
                    block(buffer)
                    setVertices(this@Builder, vertexCount, buffer, isStatic)
                }
                return EBOStep()
            }

            private fun setVertices(parent: Builder, vertexCount: Int, buffer: FloatBuffer, isStatic: Boolean) {
                parent.vertexCount = vertexCount
                parent.vbo = GL15.glGenBuffers()
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, parent.vbo)
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, if (isStatic) GL15.GL_STATIC_DRAW else GL15.GL_DYNAMIC_DRAW)
            }
        }

        inner class EBOStep {
            fun build(): VAO {
                GL30.glBindVertexArray(0)
                val parent = this@Builder
                return VAO(parent.vao, parent.mode, parent.vbo, parent.vertexCount, parent.ebo, parent.elementCount)
            }
        }
    }
}