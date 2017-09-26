package com.sotrh.lowpoly_terrain.terrain

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

class TerrainModel(val terrain: Terrain) {

    companion object {
        val FLOATS_PER_VERTEX = 6
    }

    val vao: Int = GL30.glGenVertexArrays()
    val vbo: Int
    val vertexCount: Int
    val ebo: Int
    val elementCount: Int

    init {
        GL30.glBindVertexArray(vao)

        vbo = GL15.glGenBuffers()
        ebo = GL15.glGenBuffers()

        vertexCount = terrain.size * terrain.size
        val vertices = FloatBuffer.allocate(vertexCount * FLOATS_PER_VERTEX)

        (0 until terrain.size).forEach { x ->
            (0 until terrain.size).forEach { z ->
                vertices.put(x.toFloat()).put(terrain.heightMap[x][z]).put(z.toFloat()) // position
                vertices.put(1.0f).put(terrain.heightMap[x][z]).put(1.0f) // color
            }
        }

        vertices.flip()

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW)


        fun elementFor(x: Int, z: Int): Int {
            return (x * terrain.size + z)
        }

        elementCount = (terrain.size - 1) * (terrain.size) * 2 + (terrain.size - 2) * 2
        val elements = IntBuffer.allocate(elementCount)

        (0 until terrain.size - 1).forEach { x ->
            (0 until terrain.size).forEach { z ->
                elements.put(elementFor(x, z))
                elements.put(elementFor(x + 1, z))
            }

            if (x + 2 < terrain.size) {
                elements.put(elementFor(x + 1, terrain.size - 1))
                elements.put(elementFor(x + 1, 0))
            }
        }

        elements.flip()

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, elements, GL15.GL_STATIC_DRAW)

        GL30.glBindVertexArray(0)
    }

    fun draw() {
        GL30.glBindVertexArray(vao)
        GL20.glEnableVertexAttribArray(0)
        GL20.glEnableVertexAttribArray(1)
        GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, elementCount, GL11.GL_INT,0L)
        GL30.glBindVertexArray(0)
    }

    fun delete() {
        GL20.glDisableVertexAttribArray(0)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL15.glDeleteBuffers(vbo)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
        GL15.glDeleteBuffers(ebo)

        GL30.glBindVertexArray(0)
        GL30.glDeleteVertexArrays(vao)
    }

}