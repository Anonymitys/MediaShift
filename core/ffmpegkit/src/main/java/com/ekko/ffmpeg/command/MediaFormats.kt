package com.ekko.ffmpeg.command

enum class VideoFormat(
    val extension: String,
    val displayName: String,
    val mimeType: String,
    val videoCodec: String,
    val audioCodec: String,
    val extraArgs: List<String> = emptyList()
) {
    MP4("mp4", "MP4 (H.264/AAC)", "video/mp4", "libx264", "aac",
        listOf("-movflags", "+faststart")),
    MOV("mov", "MOV (H.264/AAC)", "video/quicktime", "libx264", "aac"),
    AVI("avi", "AVI (H.264/MP3)", "video/x-msvideo", "libx264", "libmp3lame"),
    MKV("mkv", "MKV (H.264/AAC)", "video/x-matroska", "libx264", "aac"),
    WEBM("webm", "WebM (VP9/Opus)", "video/webm", "libvpx-vp9", "libopus",
        listOf("-crf", "32", "-b:v", "0"));

    companion object {
        val allExtensions = entries.map { it.extension }
        fun fromExtension(ext: String): VideoFormat? =
            entries.firstOrNull { it.extension.equals(ext, ignoreCase = true) }
    }
}

enum class AudioFormat(
    val extension: String,
    val displayName: String,
    val mimeType: String,
    val audioCodec: String,
    val extraArgs: List<String> = emptyList()
) {
    MP3("mp3", "MP3 (MPEG Audio)", "audio/mpeg", "libmp3lame"),
    AAC("aac", "AAC", "audio/aac", "aac"),
    M4A("m4a", "M4A (AAC)", "audio/mp4", "aac"),
    WAV("wav", "WAV (PCM)", "audio/wav", "pcm_s16le",
        listOf("-ar", "44100", "-ac", "2")),
    FLAC("flac", "FLAC (Lossless)", "audio/flac", "flac"),
    OGG("ogg", "OGG (Opus)", "audio/ogg", "libopus"),
    OPUS("opus", "Opus", "audio/opus", "libopus");

    companion object {
        val allExtensions = entries.map { it.extension }
        fun fromExtension(ext: String): AudioFormat? =
            entries.firstOrNull { it.extension.equals(ext, ignoreCase = true) }
    }
}

enum class ImageFormat(
    val extension: String,
    val displayName: String,
    val mimeType: String,
    val codec: String,
    val hasQualityControl: Boolean = true,
    val extraArgs: List<String> = emptyList()
) {
    JPEG("jpg", "JPEG", "image/jpeg", "mjpeg"),
    PNG("png", "PNG", "image/png", "png"),
    WEBP("webp", "WebP", "image/webp", "libwebp",
        extraArgs = listOf("-quality", "85")),
    GIF("gif", "GIF", "image/gif", "gif"),
    BMP("bmp", "BMP", "image/bmp", "bmp");

    companion object {
        val allExtensions = entries.map { it.extension }
        fun fromExtension(ext: String): ImageFormat? =
            entries.firstOrNull { it.extension.equals(ext, ignoreCase = true) }
    }
}
