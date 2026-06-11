package com.example.videobrowser.video

object VideoSpeedOptions {
    val longPressSpeed = 2f

    private val menuSpeeds = listOf(
        0.5f,
        0.75f,
        1f,
        1.25f,
        1.5f,
        2f,
        2.5f,
        3f
    )

    fun menuSpeeds(): List<Float> = menuSpeeds
}
