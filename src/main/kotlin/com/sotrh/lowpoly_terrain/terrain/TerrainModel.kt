package com.sotrh.lowpoly_terrain.terrain

import com.sotrh.lowpoly_terrain.common.*
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
                .triangleStrip()
                .vertices(terrain.size * terrain.size, FLOATS_PER_VERTEX, true) { buffer, vertexCount ->
                    (0 until terrain.size).forEach { x ->
                        (0 until terrain.size).forEach { z ->
                            buffer.put(x - terrain.size * 0.5f - 0.5f).put(terrain.heightMap[x][z]).put(z - terrain.size * 0.5f - 0.5f) // position
                            buffer.put(0.5f).put(terrain.heightMap[x][z]).put(0.5f) // color
                        }
                    }
                    buffer.flip()
                }
                .elements((terrain.size - 1) * (terrain.size) * 2 + (terrain.size - 2) * 2, true) { buffer, elementCount ->
                    fun elementFor(x: Int, z: Int): Int {
                        return (x * terrain.size + z)
                    }

                    (0 until terrain.size - 1).forEach { x ->
                        (0 until terrain.size).forEach { z ->
                            buffer.put(elementFor(x, z))
                            buffer.put(elementFor(x + 1, z))
                        }

                        if (x + 2 < terrain.size) {
                            buffer.put(elementFor(x + 1, terrain.size - 1))
                            buffer.put(elementFor(x + 1, 0))
                        }
                    }
                    buffer.flip()
                }
                .build()
) {

    companion object {
        val FLOATS_PER_VERTEX = 6
    }

    fun destroy() {
        vao.delete()
    }

}