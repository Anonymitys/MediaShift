package com.ekko.ffmpeg.engine

sealed class FfmpegResult {
    data object Success : FfmpegResult()
    data object Canceled : FfmpegResult()
    data class Failure(val message: String) : FfmpegResult()

    val isSuccess: Boolean get() = this is Success
    val isCanceled: Boolean get() = this is Canceled
}
