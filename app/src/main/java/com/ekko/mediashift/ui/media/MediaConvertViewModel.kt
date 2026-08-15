package com.ekko.mediashift.ui.media

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ekko.ffmpeg.command.AudioCommandBuilder
import com.ekko.ffmpeg.command.AudioFormat
import com.ekko.ffmpeg.command.AudioRequest
import com.ekko.ffmpeg.command.ImageCommandBuilder
import com.ekko.ffmpeg.command.ImageFormat
import com.ekko.ffmpeg.command.ImageRequest
import com.ekko.ffmpeg.command.VideoCommandBuilder
import com.ekko.ffmpeg.command.VideoFormat
import com.ekko.ffmpeg.command.VideoRequest
import com.ekko.ffmpeg.engine.FfmpegResult
import com.ekko.ffmpeg.engine.FFmpegEngine
import com.ekko.ffmpeg.engine.MediaProbe
import com.ekko.ffmpeg.path.MediaPathResolver
import com.ekko.mediashift.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConversionUiState(
    val isConverting: Boolean = false,
    val progress: Float = 0f,
    val result: ConversionResult? = null
)

sealed class ConversionResult {
    data class Success(val outputPath: String) : ConversionResult()
    data class Failure(val message: String) : ConversionResult()
    data object Canceled : ConversionResult()
}

class MediaConvertViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val engine = ServiceLocator.ffmpegEngine

    val mediaKind: MediaKind = savedStateHandle.get<String>("mediaKind")
        ?.let { MediaKind.parse(it) } ?: MediaKind.VIDEO

    private val _selectedFiles = MutableStateFlow<List<SelectedMedia>>(emptyList())
    val selectedFiles: StateFlow<List<SelectedMedia>> = _selectedFiles.asStateFlow()

    private val _uiState = MutableStateFlow(ConversionUiState())
    val uiState: StateFlow<ConversionUiState> = _uiState.asStateFlow()

    // Format selections
    private val _selectedVideoFormat = MutableStateFlow(VideoFormat.MP4)
    val selectedVideoFormat: StateFlow<VideoFormat> = _selectedVideoFormat.asStateFlow()

    private val _selectedAudioFormat = MutableStateFlow(AudioFormat.MP3)
    val selectedAudioFormat: StateFlow<AudioFormat> = _selectedAudioFormat.asStateFlow()

    private val _selectedImageFormat = MutableStateFlow(ImageFormat.JPEG)
    val selectedImageFormat: StateFlow<ImageFormat> = _selectedImageFormat.asStateFlow()

    fun addFiles(uris: List<Uri>) {
        val context = getApplication<Application>()
        val newFiles = uris.mapNotNull { uri ->
            try {
                val name = getFileName(context, uri)
                SelectedMedia(uri = uri, name = name)
            } catch (e: Exception) {
                null
            }
        }
        _selectedFiles.update { current ->
            val existingUris = current.map { it.uri }.toSet()
            current + newFiles.filter { it.uri !in existingUris }
        }
    }

    fun removeFile(media: SelectedMedia) {
        _selectedFiles.update { it.filter { f -> f.uri != media.uri } }
    }

    fun clearFiles() {
        _selectedFiles.value = emptyList()
    }

    fun setVideoFormat(format: VideoFormat) {
        _selectedVideoFormat.value = format
    }

    fun setAudioFormat(format: AudioFormat) {
        _selectedAudioFormat.value = format
    }

    fun setImageFormat(format: ImageFormat) {
        _selectedImageFormat.value = format
    }

    fun startConversion(
        videoBitrateKbps: Int? = null,
        width: Int? = null,
        height: Int? = null,
        audioBitrateKbps: Int = 192,
        imageQuality: Int = 85
    ) {
        if (_selectedFiles.value.isEmpty()) return
        if (_uiState.value.isConverting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isConverting = true, progress = 0f, result = null) }

            val context = getApplication<Application>()
            val resolver = MediaPathResolver
            var lastResult: FfmpegResult? = null

            for (media in _selectedFiles.value) {
                if (!_uiState.value.isConverting) break

                val inputPath = resolver.resolveInput(context, media.uri)
                val result = when (mediaKind) {
                    MediaKind.VIDEO -> {
                        val format = _selectedVideoFormat.value
                        val outputPath = resolver.buildOutputPath(
                            context, "videos",
                            resolver.generateFileName(media.name, format.extension)
                        )
                        val request = VideoRequest(
                            inputPath = inputPath,
                            outputPath = outputPath,
                            format = format,
                            videoBitrateKbps = videoBitrateKbps,
                            width = width,
                            height = height,
                            audioBitrateKbps = audioBitrateKbps
                        )
                        val args = VideoCommandBuilder.build(request)
                        engine.execute(args) { progress ->
                            _uiState.update { it.copy(progress = progress) }
                        }
                    }
                    MediaKind.AUDIO -> {
                        val format = _selectedAudioFormat.value
                        val outputPath = resolver.buildOutputPath(
                            context, "audio",
                            resolver.generateFileName(media.name, format.extension)
                        )
                        val request = AudioRequest(
                            inputPath = inputPath,
                            outputPath = outputPath,
                            format = format,
                            audioBitrateKbps = audioBitrateKbps
                        )
                        val args = AudioCommandBuilder.build(request)
                        engine.execute(args) { progress ->
                            _uiState.update { it.copy(progress = progress) }
                        }
                    }
                    MediaKind.IMAGE -> {
                        val format = _selectedImageFormat.value
                        val outputPath = resolver.buildOutputPath(
                            context, "images",
                            resolver.generateFileName(media.name, format.extension)
                        )
                        val request = ImageRequest(
                            inputPath = inputPath,
                            outputPath = outputPath,
                            format = format,
                            quality = imageQuality,
                            width = width,
                            height = height
                        )
                        val args = ImageCommandBuilder.build(request)
                        engine.execute(args) { progress ->
                            _uiState.update { it.copy(progress = progress) }
                        }
                    }
                }
                lastResult = result
                if (result !is FfmpegResult.Success) break
            }

            val finalResult = lastResult
            _uiState.update {
                it.copy(
                    isConverting = false,
                    progress = 1f,
                    result = when (finalResult) {
                        is FfmpegResult.Success -> ConversionResult.Success("Conversion done")
                        is FfmpegResult.Canceled -> ConversionResult.Canceled
                        is FfmpegResult.Failure -> ConversionResult.Failure(finalResult.message)
                        null -> ConversionResult.Canceled
                    }
                )
            }
        }
    }

    fun cancelConversion() {
        // Cancel is handled via coroutine cancellation in the engine
        _uiState.update { it.copy(isConverting = false) }
    }

    fun dismissResult() {
        _uiState.update { it.copy(result = null) }
    }

    private fun getFileName(context: android.content.Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return it.getString(nameIndex) ?: uri.lastPathSegment ?: "unknown"
                }
            }
        }
        return uri.lastPathSegment ?: "unknown"
    }
}
