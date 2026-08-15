package com.ekko.mediashift.ui.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ekko.ffmpeg.command.AudioFormat
import com.ekko.ffmpeg.command.ImageFormat
import com.ekko.ffmpeg.command.VideoFormat
import com.ekko.mediashift.R
import com.ekko.mediashift.databinding.FragmentConversionBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MediaConvertFragment : Fragment() {

    private var _binding: FragmentConversionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MediaConvertViewModel by viewModels()

    private lateinit var adapter: MediaFileAdapter

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addFiles(uris)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFormatDropdowns()
        setupOptions()
        setupButtons()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = MediaFileAdapter { media -> viewModel.removeFile(media) }
        binding.fileRecyclerView.adapter = adapter
    }

    private fun setupFormatDropdowns() {
        val kind = viewModel.mediaKind
        val formats: List<String>
        val dropdown = binding.formatDropdown

        when (kind) {
            MediaKind.VIDEO -> {
                formats = VideoFormat.entries.map { it.displayName }
                val adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, formats)
                dropdown.setAdapter(adapter)
                dropdown.setText(VideoFormat.MP4.displayName, false)
                dropdown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.setVideoFormat(VideoFormat.entries[position])
                }
            }
            MediaKind.AUDIO -> {
                formats = AudioFormat.entries.map { it.displayName }
                val adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, formats)
                dropdown.setAdapter(adapter)
                dropdown.setText(AudioFormat.MP3.displayName, false)
                dropdown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.setAudioFormat(AudioFormat.entries[position])
                }
            }
            MediaKind.IMAGE -> {
                formats = ImageFormat.entries.map { it.displayName }
                val adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, formats)
                dropdown.setAdapter(adapter)
                dropdown.setText(ImageFormat.JPEG.displayName, false)
                dropdown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.setImageFormat(ImageFormat.entries[position])
                }
            }
        }
    }

    private fun setupOptions() {
        val kind = viewModel.mediaKind
        binding.tabTitle.text = kind.displayName

        // Show/hide option groups
        binding.videoOptionsGroup.visibility =
            if (kind == MediaKind.VIDEO) View.VISIBLE else View.GONE
        binding.audioOptionsGroup.visibility =
            if (kind == MediaKind.AUDIO) View.VISIBLE else View.GONE
        binding.imageOptionsGroup.visibility =
            if (kind == MediaKind.IMAGE) View.VISIBLE else View.GONE

        // Video bitrate slider
        binding.videoBitrateSlider.addOnChangeListener { _, value, _ ->
            binding.videoBitrateLabel.text = "${value.toInt()} kbps"
        }

        // Audio bitrate slider
        binding.audioBitrateSlider.addOnChangeListener { _, value, _ ->
            binding.audioBitrateLabel.text = "${value.toInt()} kbps"
        }

        // Image quality slider
        binding.imageQualitySlider.addOnChangeListener { _, value, _ ->
            binding.imageQualityLabel.text = "${value.toInt()}%"
        }
    }

    private fun setupButtons() {
        binding.addFilesButton.setOnClickListener {
            filePickerLauncher.launch(
                arrayOf(viewModel.mediaKind.mimeType)
            )
        }

        binding.convertButton.setOnClickListener {
            val kind = viewModel.mediaKind
            val width = when (kind) {
                MediaKind.VIDEO -> binding.resWidthInput.text?.toString()?.toIntOrNull()
                MediaKind.IMAGE -> binding.imgWidthInput.text?.toString()?.toIntOrNull()
                else -> null
            }
            val height = when (kind) {
                MediaKind.VIDEO -> binding.resHeightInput.text?.toString()?.toIntOrNull()
                MediaKind.IMAGE -> binding.imgHeightInput.text?.toString()?.toIntOrNull()
                else -> null
            }
            viewModel.startConversion(
                videoBitrateKbps = binding.videoBitrateSlider.value.toInt().takeIf { kind == MediaKind.VIDEO },
                width = width,
                height = height,
                audioBitrateKbps = binding.audioBitrateSlider.value.toInt(),
                imageQuality = binding.imageQualitySlider.value.toInt()
            )
        }

        binding.cancelButton.setOnClickListener {
            viewModel.cancelConversion()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Selected files
                launch {
                    viewModel.selectedFiles.collectLatest { files ->
                        adapter.submitList(files)
                        binding.emptyStateText.visibility =
                            if (files.isEmpty()) View.VISIBLE else View.GONE
                        binding.fileRecyclerView.visibility =
                            if (files.isEmpty()) View.GONE else View.VISIBLE
                        binding.optionsCard.visibility =
                            if (files.isEmpty()) View.GONE else View.VISIBLE
                    }
                }

                // UI state
                launch {
                    viewModel.uiState.collectLatest { state ->
                        if (state.isConverting) {
                            binding.convertButton.visibility = View.GONE
                            binding.progressGroup.visibility = View.VISIBLE
                            binding.progressBar.isIndeterminate = state.progress == 0f
                            binding.progressBar.progress =
                                (state.progress * 100).toInt().coerceIn(0, 100)
                        } else {
                            binding.convertButton.visibility = View.VISIBLE
                            binding.progressGroup.visibility = View.GONE
                        }

                        state.result?.let { result ->
                            val message = when (result) {
                                is ConversionResult.Success -> getString(R.string.conversion_done)
                                is ConversionResult.Failure -> getString(R.string.conversion_failed) + ": ${result.message}"
                                is ConversionResult.Canceled -> getString(R.string.conversion_canceled)
                            }
                            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                            viewModel.dismissResult()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
