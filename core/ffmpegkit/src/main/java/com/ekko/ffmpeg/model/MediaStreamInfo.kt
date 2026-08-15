package com.ekko.ffmpeg.model

data class MediaStreamInfo(
    val index: Int,
    val type: String,       // "video", "audio", "subtitle", etc.
    val codec: String,
    val width: Int = 0,
    val height: Int = 0,
    val sampleRate: Int = 0,
    val channels: Int = 0,
    val bitrate: Long = 0,
    val durationMs: Long = 0
)
