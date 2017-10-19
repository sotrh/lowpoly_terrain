package com.sotrh.lowpoly_terrain.terrain

import java.util.Random

/**
 * Generates the heights of the terrain. Check out tutorial 37 for more
 * info: https://youtu.be/qChQrNWU9Xw
 *
 * @author Karl
 */
class PerlinNoise {

    private val roughness: Float
    private val octaves: Int
    val seed: Int
    val amplitude: Float

    constructor(seed: Int, octaves: Int, amplitude: Float, roughness: Float) {
        this.seed = seed
        this.octaves = octaves
        this.amplitude = amplitude
        this.roughness = roughness
    }

    constructor(octaves: Int, amplitude: Float, roughness: Float) {
        this.seed = Random().nextInt(1000000000)
        this.octaves = octaves
        this.amplitude = amplitude
        this.roughness = roughness
    }

    fun getPerlinNoise(x: Int, y: Int): Float {
        var total = 0f
        val d = Math.pow(2.0, (octaves - 1).toDouble()).toFloat()
        for (i in 0 until octaves) {
            val freq = (Math.pow(2.0, i.toDouble()) / d).toFloat()
            val amp = Math.pow(roughness.toDouble(), i.toDouble()).toFloat() * amplitude
            total += getInterpolatedNoise(x * freq, y * freq) * amp
        }
        return total
    }

    private fun getSmoothNoise(x: Int, y: Int): Float {
        val corners = (getNoise(x - 1, y - 1) + getNoise(x + 1, y - 1) + getNoise(x - 1, y + 1)
                + getNoise(x + 1, y + 1)) / 16f
        val sides = (getNoise(x - 1, y) + getNoise(x + 1, y) + getNoise(x, y - 1) + getNoise(x, y + 1)) / 8f
        val center = getNoise(x, y) / 4f
        return corners + sides + center
    }

    private fun getNoise(x: Int, y: Int): Float {
        return Random((x * 49632 + y * 325176 + seed).toLong()).nextFloat() * 2f - 1f
    }

    private fun getInterpolatedNoise(x: Float, y: Float): Float {
        val intX = x.toInt()
        val fracX = x - intX
        val intY = y.toInt()
        val fracY = y - intY

        val v1 = getSmoothNoise(intX, intY)
        val v2 = getSmoothNoise(intX + 1, intY)
        val v3 = getSmoothNoise(intX, intY + 1)
        val v4 = getSmoothNoise(intX + 1, intY + 1)
        val i1 = interpolate(v1, v2, fracX)
        val i2 = interpolate(v3, v4, fracX)
        return interpolate(i1, i2, fracY)
    }

    private fun interpolate(a: Float, b: Float, blend: Float): Float {
        val theta = blend * Math.PI
        val f = ((1f - Math.cos(theta)) * 0.5f).toFloat()
        return a * (1 - f) + b * f
    }

}