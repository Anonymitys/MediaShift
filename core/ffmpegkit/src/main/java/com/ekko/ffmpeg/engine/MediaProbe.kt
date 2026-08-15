package com.ekko.ffmpeg.engine

import com.antonkarpenko.ffmpegkit.FFprobeKit
import com.ekko.ffmpeg.model.MediaInfo
import com.ekko.ffmpeg.model.MediaStreamInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaProbe {

    suspend fun probe(path: String): MediaInfo = withContext(Dispatchers.IO) {
        val session = FFprobeKit.getMediaInformation(path)
        val info = session.mediaInformation
            ?: return@withContext MediaInfo(path = path)

        val streams = info.streams?.mapNotNull { stream ->
            stream ?: return@mapNotNull null
            MediaStreamInfo(
                index = stream.index.toInt(),
                type = stream.type ?: "unknown",
                codec = stream.codec ?: "unknown",
                width = (stream.width ?: 0).toInt(),
                height = (stream.height ?: 0).toInt(),
                sampleRate = stream.sampleRate?.toIntOrNull() ?: 0,
                channels = stream.channelLayout?.let { 2 } ?: 0,
                bitrate = stream.bitrate?.toLongOrNull() ?: 0L,
                durationMs = 0L
            )
        } ?: emptyList()

        val durationMs = info.duration?.let {
            // Duration string like "123.456000" → parse as seconds → ms
            (it.toDoubleOrNull()?.times(1000))?.toLong()
        } ?: 0L

        MediaInfo(
            path = path,
            durationMs = durationMs,
            bitrate = info.bitrate?.toLongOrNull() ?: 0L,
            sizeBytes = info.size?.toLongOrNull() ?: 0L,
            streams = streams
        )
    }
}
