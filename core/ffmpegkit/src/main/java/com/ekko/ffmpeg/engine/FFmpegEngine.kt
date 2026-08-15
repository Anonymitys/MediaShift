package com.ekko.ffmpeg.engine

import com.antonkarpenko.ffmpegkit.FFmpegKit
import com.antonkarpenko.ffmpegkit.FFmpegSessionCompleteCallback
import com.antonkarpenko.ffmpegkit.StatisticsCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object FFmpegEngine {

    suspend fun execute(
        arguments: List<String>,
        onProgress: ((Float) -> Unit)? = null
    ): FfmpegResult = suspendCancellableCoroutine { continuation ->
        var sessionId = -1L
        var completed = false

        val completeCallback = FFmpegSessionCompleteCallback { session ->
            if (completed) return@FFmpegSessionCompleteCallback
            completed = true
            val result = when {
                session.returnCode?.isValueSuccess == true -> FfmpegResult.Success
                session.returnCode?.isValueCancel == true -> FfmpegResult.Canceled
                else -> FfmpegResult.Failure(
                    session.failStackTrace ?: session.output ?: "Unknown error"
                )
            }
            onProgress?.invoke(1f)
            continuation.resume(result)
        }

        val statisticsCallback = StatisticsCallback { stats ->
            onProgress?.invoke(
                stats.time.toFloat().coerceIn(0f, 1f)
            )
        }

        val session = FFmpegKit.executeWithArgumentsAsync(
            arguments.toTypedArray(),
            completeCallback,
            null,
            statisticsCallback
        )
        sessionId = session.sessionId

        continuation.invokeOnCancellation {
            if (!completed) {
                FFmpegKit.cancel(sessionId)
            }
        }
    }

    fun cancel(sessionId: Long) {
        FFmpegKit.cancel(sessionId)
    }
}
