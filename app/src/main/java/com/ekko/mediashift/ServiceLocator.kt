package com.ekko.mediashift

import com.ekko.ffmpeg.engine.FFmpegEngine

object ServiceLocator {
    val ffmpegEngine: FFmpegEngine
        get() = FFmpegEngine
}
