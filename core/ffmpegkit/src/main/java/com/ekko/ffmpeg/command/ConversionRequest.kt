package com.ekko.ffmpeg.command

data class VideoRequest(
    val inputPath: String,
    val outputPath: String,
    val format: VideoFormat = VideoFormat.MP4,
    val videoBitrateKbps: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val crf: Int = 23,
    val audioBitrateKbps: Int = 128,
    val preset: String = "medium"
)

data class AudioRequest(
    val inputPath: String,
    val outputPath: String,
    val format: AudioFormat = AudioFormat.MP3,
    val audioBitrateKbps: Int = 192,
    val sampleRate: Int? = null,
    val channels: Int? = null
)

data class ImageRequest(
    val inputPath: String,
    val outputPath: String,
    val format: ImageFormat = ImageFormat.JPEG,
    val quality: Int = 85,
    val width: Int? = null,
    val height: Int? = null
)
