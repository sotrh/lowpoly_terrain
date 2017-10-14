package com.sotrh.lowpoly_terrain.terrain

import com.sotrh.lowpoly_terrain.common.*
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

class TerrainModel(private val terrain: Terrain) : Model(
        Shader(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER),
        VAO.Builder()
                .triangles()
                .vertices(terrain.size * terrain.size, FLOATS_PER_VERTEX, true) { buffer, vertexCount ->
                    (0 until terrain.size).forEach { x ->
                        (0 until terrain.size).forEach { z ->
                            val height = terrain.heightMap[x][z]

                            // position
                            buffer.put(x - terrain.size * 0.5f - 0.5f).put(height).put(z - terrain.size * 0.5f - 0.5f)

                            // color
                            val colorIndex = Math.min((VERTEX_COLORS.size * height).toInt(), VERTEX_COLORS.size - 1)
                            val vertexColor = VERTEX_COLORS[colorIndex]

                            println("x = $x, z = $z, height = $height")
                            println("colorIndex = $colorIndex")
                            println("vertexColor = $vertexColor")

                            buffer.put(vertexColor.x).put(vertexColor.y).put(vertexColor.z)
                        }
                    }
                    buffer.flip()
                }

                // element count for triangles:
                // 2 triangles per square
                // 3 elements per triangle
                // 6 elements per square
                // (terrain.size - 1) ^ 2 squares
                // total elements = 6 * (terrain.size - 1) ^ 2
                .elements(6 * (terrain.size - 1) * (terrain.size - 1), true) { buffer, _ ->
                    fun elementFor(x: Int, z: Int): Int {
                        return (x * terrain.size + z)
                    }

                    // since triangles are per square, has to skip the last vertices of the mesh
                    (0 .. terrain.size - 2).forEach { x ->
                        (0 .. terrain.size - 2).forEach { z ->

                            // 1st triangle
                            buffer.put(elementFor(x, z)) // bottom left
                            buffer.put(elementFor(x + 1, z)) // bottom right
                            buffer.put(elementFor(x + 1, z + 1)) // top right

                            // 2nd triangle
                            buffer.put(elementFor(x + 1, z + 1)) // top right
                            buffer.put(elementFor(x, z + 1)) // top left
                            buffer.put(elementFor(x, z)) // bottom left
                        }
                    }
                    buffer.flip()
                }
                .build()
) {

    companion object {
        val FLOATS_PER_VERTEX = 6

        // bottom to top
        val VERTEX_COLORS = arrayOf(Vector3f(0f, 0f, 1f), Vector3f(0.5f, 0.5f, 0f), Vector3f(0f, 1f, 0f), Vector3f(1f, 1f, 1f))
    }

    fun destroy() {
        vao.delete()
    }

}