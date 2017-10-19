package com.sotrh.lowpoly_terrain.terrain

import org.joml.Math
import java.util.*

class Terrain(val size: Int) {

    init {
        if (size <= 1) throw IllegalArgumentException("Terrain size was $size. Terrain size must be >= 2")
    }

    val heightMap = Array(size) {
        kotlin.FloatArray(size)
    }

    object Builder {
        fun random(size: Int): Terrain {
            val noise = PerlinNoise(3, 10f, 0.35f)
            val terrain = Terrain(size)
            (0 until size).forEach { x ->
                (0 until size).forEach { z ->
                    terrain.heightMap[x][z] = noise.getPerlinNoise(x, z) + 5f
                }
            }
            return terrain
        }

        fun sin(size: Int): Terrain {
            val terrain = Terrain(size)
            (0 until size).forEach { x ->
                (0 until size).forEach { z ->
                    terrain.heightMap[x][z] = Math.sin(x.toDouble() / size * 2 * Math.PI).toFloat() * 0.5f + 0.5f
                }
            }
            return terrain
        }
    }
}