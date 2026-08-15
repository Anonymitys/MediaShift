package com.ekko.ffmpeg.command

object ImageCommandBuilder {

    fun build(request: ImageRequest): List<String> {
        val args = mutableListOf<String>()

        // Base
        args.addAll(listOf("-y", "-hide_banner", "-nostdin"))
        args.addAll(listOf("-i", request.inputPath))

        // Resolution scaling
        val scaleW = request.width ?: -2
        val scaleH = request.height ?: -2
        if (scaleW > 0 || scaleH > 0) {
            args.addAll(listOf("-vf", "scale=$scaleW:$scaleH"))
        }

        // Quality / compression level
        when (request.format) {
            ImageFormat.JPEG -> {
                // JPEG quality 2-31, lower is better
                val q = ((31 - 2) * (100 - request.quality) / 100f + 2).toInt()
                    .coerceIn(2, 31)
                args.addAll(listOf("-q:v", q.toString()))
            }
            ImageFormat.WEBP -> {
                args.addAll(listOf("-quality", request.quality.toString()))
            }
            ImageFormat.PNG -> {
                // PNG compression level 0-100 → ffmpeg 0-9
                val level = (request.quality / 11.1).toInt().coerceIn(0, 9)
                args.addAll(listOf("-compression_level", level.toString()))
            }
            else -> {
                // GIF, BMP: no quality control, just use format defaults
            }
        }

        // Format-specific extras
        args.addAll(request.format.extraArgs)

        // Output
        args.add(request.outputPath)

        return args
    }
}
