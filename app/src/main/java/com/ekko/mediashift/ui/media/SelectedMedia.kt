package com.ekko.mediashift.ui.media

import android.net.Uri

data class SelectedMedia(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long = 0,
    val durationMs: Long = 0,
    val width: Int = 0,
    val height: Int = 0
) {
    val sizeFormatted: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1 -> "%.1f MB".format(mb)
                kb >= 1 -> "%.1f KB".format(kb)
                else -> "$sizeBytes B"
            }
        }

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

    val resolutionFormatted: String
        get() = if (width > 0 && height > 0) "${width}×${height}" else ""
}
