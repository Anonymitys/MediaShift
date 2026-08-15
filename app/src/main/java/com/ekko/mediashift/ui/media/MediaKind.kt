package com.ekko.mediashift.ui.media

import com.ekko.ffmpeg.command.AudioFormat
import com.ekko.ffmpeg.command.ImageFormat
import com.ekko.ffmpeg.command.VideoFormat

enum class MediaKind(
    val mimeType: String,
    val displayName: String
) {
    VIDEO("video/*", "Video"),
    AUDIO("audio/*", "Audio"),
    IMAGE("image/*", "Image");

    val videoFormats: List<VideoFormat>
        get() = if (this == VIDEO) VideoFormat.entries else emptyList()

    val audioFormats: List<AudioFormat>
        get() = if (this == AUDIO) AudioFormat.entries else emptyList()

    val imageFormats: List<ImageFormat>
        get() = if (this == IMAGE) ImageFormat.entries else emptyList()

    companion object {
        fun parse(value: String): MediaKind =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: VIDEO
    }
}
