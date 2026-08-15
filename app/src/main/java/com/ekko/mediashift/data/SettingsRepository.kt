package com.ekko.mediashift.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

data class AppSettings(
    val outputDirTreeUri: String = "",
    val defaultVideoFormat: String = "MP4",
    val defaultAudioFormat: String = "MP3",
    val defaultImageFormat: String = "JPEG",
    val defaultVideoQuality: Int = 23,
    val defaultAudioBitrate: Int = 192,
    val defaultImageQuality: Int = 85
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mediashift_settings", Context.MODE_PRIVATE)

    fun getSettings(): AppSettings = AppSettings(
        outputDirTreeUri = prefs.getString(KEY_OUTPUT_DIR_URI, "") ?: "",
        defaultVideoFormat = prefs.getString(KEY_DEFAULT_VIDEO_FORMAT, "MP4") ?: "MP4",
        defaultAudioFormat = prefs.getString(KEY_DEFAULT_AUDIO_FORMAT, "MP3") ?: "MP3",
        defaultImageFormat = prefs.getString(KEY_DEFAULT_IMAGE_FORMAT, "JPEG") ?: "JPEG",
        defaultVideoQuality = prefs.getInt(KEY_DEFAULT_VIDEO_QUALITY, 23),
        defaultAudioBitrate = prefs.getInt(KEY_DEFAULT_AUDIO_BITRATE, 192),
        defaultImageQuality = prefs.getInt(KEY_DEFAULT_IMAGE_QUALITY, 85)
    )

    fun setOutputDirUri(uri: String) {
        prefs.edit { putString(KEY_OUTPUT_DIR_URI, uri) }
    }

    fun clearOutputDirectory() {
        prefs.edit { remove(KEY_OUTPUT_DIR_URI) }
    }

    fun setDefaultVideoFormat(format: String) {
        prefs.edit { putString(KEY_DEFAULT_VIDEO_FORMAT, format) }
    }

    fun setDefaultAudioFormat(format: String) {
        prefs.edit { putString(KEY_DEFAULT_AUDIO_FORMAT, format) }
    }

    fun setDefaultImageFormat(format: String) {
        prefs.edit { putString(KEY_DEFAULT_IMAGE_FORMAT, format) }
    }

    fun setDefaultVideoQuality(quality: Int) {
        prefs.edit { putInt(KEY_DEFAULT_VIDEO_QUALITY, quality) }
    }

    fun setDefaultAudioBitrate(bitrate: Int) {
        prefs.edit { putInt(KEY_DEFAULT_AUDIO_BITRATE, bitrate) }
    }

    fun setDefaultImageQuality(quality: Int) {
        prefs.edit { putInt(KEY_DEFAULT_IMAGE_QUALITY, quality) }
    }

    companion object {
        private const val KEY_OUTPUT_DIR_URI = "output_dir_tree_uri"
        private const val KEY_DEFAULT_VIDEO_FORMAT = "default_video_format"
        private const val KEY_DEFAULT_AUDIO_FORMAT = "default_audio_format"
        private const val KEY_DEFAULT_IMAGE_FORMAT = "default_image_format"
        private const val KEY_DEFAULT_VIDEO_QUALITY = "default_video_quality"
        private const val KEY_DEFAULT_AUDIO_BITRATE = "default_audio_bitrate"
        private const val KEY_DEFAULT_IMAGE_QUALITY = "default_image_quality"
    }
}
