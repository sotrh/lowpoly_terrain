package com.sotrh.lowpoly_terrain.terrain

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
            val random = Random()
            val terrain = Terrain(size)
            (0 until size).forEach { x ->
                (0 until size).forEach { z ->
                    terrain.heightMap[x][z] = random.nextFloat()
                }
            }
            println("Made it past the loop")
            return terrain
        }
    }
}