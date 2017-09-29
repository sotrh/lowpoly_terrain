package com.sotrh.lowpoly_terrain.common

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import java.nio.FloatBuffer
import java.nio.IntBuffer

class VAO(val id: Int, val mode: Int, val vbo: Int, val vertexCount: Int, val ebo: Int, val elementCount: Int) {

    private var isDeleted = false
    private var isBound = false

    fun bind() {
        assertNotDeleted()
        assertNotBound()
        isBound = true
        GL30.glBindVertexArray(id)
    }

    fun unbind() {
        assertNotDeleted()
        assertBound()
        isBound = false
        GL30.glBindVertexArray(id)
    }

    inline fun doBinded(block: (VAO) -> Unit) {
        bind()
        block(this)
        unbind()
    }

    fun draw() {
        assertBound()
        if (ebo <= 0 || elementCount <= 0) {
            GL11.glDrawArrays(mode, 0, vertexCount)
        } else {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
            GL11.glDrawElements(mode, elementCount, GL11.GL_UNSIGNED_INT, 0L)
        }
    }

    fun delete() {
        assertNotDeleted()

        bind()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL15.glDeleteBuffers(vbo)
        if (ebo != 0) {
            GL15.glDeleteBuffers(ebo)
        }
        unbind()
        GL30.glDeleteVertexArrays(id)

        isDeleted = true
    }

    private fun assertNotDeleted() {
        if (isDeleted) throw IllegalStateException("This VAO has been deleted")
    }

    private fun assertBound() {
        if (!isBound) throw IllegalStateException("This VAO ($id) needs to be bound before it is used.")
    }

    private fun assertNotBound() {
        if (isBound) throw IllegalStateException("This VAO ($id) has already been bound.")
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
                val parent = this@Builder
                parent.vertexCount = vertexCount
                parent.vbo = GL15.glGenBuffers()
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, parent.vbo)
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, if (isStatic) GL15.GL_STATIC_DRAW else GL15.GL_DYNAMIC_DRAW)
                return EBOStep()
            }

            fun vertices(vertexCount: Int, floatsPerVertex: Int, isStatic: Boolean, block: (buffer: FloatBuffer, vertexCount: Int) -> Unit): EBOStep {
                val buffer = BufferUtils.createFloatBuffer(vertexCount * floatsPerVertex)
                block(buffer, vertexCount)
                return vertices(vertexCount, buffer, isStatic)
            }
        }

        inner class EBOStep {
            fun noElements(): FinalStep {
                ebo = iNULL
                elementCount = 0
                return FinalStep()
            }

            fun elements(elementCount: Int, buffer: IntBuffer, isStatic: Boolean = true): FinalStep {
                val parent = this@Builder
                parent.elementCount = elementCount
                parent.ebo = GL15.glGenBuffers()
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, parent.ebo)
                GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, if (isStatic) GL15.GL_STATIC_DRAW else GL15.GL_DYNAMIC_DRAW)
                return FinalStep()
            }

            fun elements(elementCount: Int, isStatic: Boolean, block: (buffer: IntBuffer, elementCount: Int) -> Unit): FinalStep {
                val buffer = BufferUtils.createIntBuffer(elementCount)
                block(buffer, elementCount)
                return elements(elementCount, buffer, isStatic)
            }
        }

        inner class FinalStep {
            fun build(): VAO {
                GL30.glBindVertexArray(0)

                val parent = this@Builder
                return VAO(parent.vao, parent.mode, parent.vbo, parent.vertexCount, parent.ebo, parent.elementCount)
            }
        }
    }
}