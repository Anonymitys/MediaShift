package com.ekko.ffmpeg.command

object AudioCommandBuilder {

    fun build(request: AudioRequest): List<String> {
        val args = mutableListOf<String>()

        // Base
        args.addAll(listOf("-y", "-hide_banner", "-nostdin"))
        args.addAll(listOf("-i", request.inputPath))

        // No video
        args.add("-vn")

        // Audio codec
        args.addAll(listOf("-c:a", request.format.audioCodec))

        // Bitrate
        args.addAll(listOf("-b:a", "${request.audioBitrateKbps}k"))

        // Sample rate
        if (request.sampleRate != null && request.sampleRate > 0) {
            args.addAll(listOf("-ar", request.sampleRate.toString()))
        }

        // Channels
        if (request.channels != null && request.channels > 0) {
            args.addAll(listOf("-ac", request.channels.toString()))
        }

        // Format-specific extras
        args.addAll(request.format.extraArgs)

        // Output
        args.add(request.outputPath)

        return args
    }
}
