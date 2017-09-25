package com.sotrh.lowpoly_terrain.common

inline fun <T: AutoCloseable> T.use(block: (resource: T) -> Unit) {
    block(this)
    this.close()
}