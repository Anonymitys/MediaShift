package com.ekko.ffmpeg.path

import android.content.Context
import android.net.Uri
import com.antonkarpenko.ffmpegkit.FFmpegKitConfig

object MediaPathResolver {

    /**
     * Resolve a content:// URI to an FFmpeg-readable SAF path.
     */
    fun resolveInput(context: Context, uri: Uri): String {
        return if (uri.scheme == "content") {
            FFmpegKitConfig.getSafParameterForRead(context, uri)
        } else {
            uri.path ?: uri.toString()
        }
    }

    /**
     * Resolve a content:// URI for writing via SAF.
     */
    fun resolveOutput(context: Context, uri: Uri): String {
        return if (uri.scheme == "content") {
            FFmpegKitConfig.getSafParameterForWrite(context, uri)
        } else {
            uri.path ?: uri.toString()
        }
    }

    /**
     * Build an output path for a file in the app's external files directory.
     */
    fun buildOutputPath(
        context: Context,
        directory: String,
        fileName: String
    ): String {
        val dir = java.io.File(context.getExternalFilesDir(null), directory)
        if (!dir.exists()) dir.mkdirs()
        return java.io.File(dir, fileName).absolutePath
    }

    /**
     * Generate a unique output file name with the given extension.
     */
    fun generateFileName(baseName: String, extension: String): String {
        val safeName = baseName.replace(Regex("[^a-zA-Z0-9._\\-]"), "_")
            .substringBeforeLast(".")
        val timestamp = System.currentTimeMillis()
        return "${safeName}_$timestamp.$extension"
    }
}
