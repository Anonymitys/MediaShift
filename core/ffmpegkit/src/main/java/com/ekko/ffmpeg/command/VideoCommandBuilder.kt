package com.ekko.ffmpeg.command

object VideoCommandBuilder {

    fun build(request: VideoRequest): List<String> {
        val args = mutableListOf<String>()

        // Base
        args.addAll(listOf("-y", "-hide_banner", "-nostdin"))
        args.addAll(listOf("-i", request.inputPath))

        // Map streams (audio optional in case of audio-less video)
        args.addAll(listOf("-map", "0:v:0"))
        args.add("-map")
        args.add("0:a:0?")

        // Video codec
        args.addAll(listOf("-c:v", request.format.videoCodec))

        // CRF (quality)
        if (request.format.videoCodec == "libx264") {
            args.addAll(listOf("-crf", request.crf.toString()))
        }

        // Preset
        if (request.format.videoCodec == "libx264" || request.format.videoCodec == "libvpx-vp9") {
            args.addAll(listOf("-preset", request.preset))
        }

        // Bitrate
        if (request.videoBitrateKbps != null && request.videoBitrateKbps > 0) {
            val bitrate = "${request.videoBitrateKbps}k"
            args.addAll(listOf("-b:v", bitrate))
            args.addAll(listOf("-maxrate", bitrate))
            args.addAll(listOf("-bufsize", "${request.videoBitrateKbps * 2}k"))
        }

        // Resolution scaling
        val scaleW = request.width ?: -2
        val scaleH = request.height ?: -2
        if (scaleW > 0 || scaleH > 0) {
            args.addAll(listOf("-vf", "scale=$scaleW:$scaleH"))
        }

        // Audio codec
        args.addAll(listOf("-c:a", request.format.audioCodec))
        args.addAll(listOf("-b:a", "${request.audioBitrateKbps}k"))

        // Format-specific extras
        args.addAll(request.format.extraArgs)

        // Output
        args.add(request.outputPath)

        return args
    }
}
