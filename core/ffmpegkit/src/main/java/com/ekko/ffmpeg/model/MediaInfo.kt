package com.ekko.ffmpeg.model

data class MediaInfo(
    val path: String,
    val durationMs: Long = 0,
    val bitrate: Long = 0,
    val sizeBytes: Long = 0,
    val streams: List<MediaStreamInfo> = emptyList()
) {
    val videoStreams: List<MediaStreamInfo>
        get() = streams.filter { it.type == "video" }

    val audioStreams: List<MediaStreamInfo>
        get() = streams.filter { it.type == "audio" }

    val primaryVideo: MediaStreamInfo?
        get() = videoStreams.firstOrNull()

    val primaryAudio: MediaStreamInfo?
        get() = audioStreams.firstOrNull()

    val hasVideo: Boolean get() = videoStreams.isNotEmpty()
    val hasAudio: Boolean get() = audioStreams.isNotEmpty()

    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%d:%02d".format(minutes, seconds)
            }
        }
}
