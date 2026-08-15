package com.ekko.mediashift.ui.settings

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
import com.ekko.mediashift.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    private val dirPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.setOutputDirectory(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFormatDropdowns()
        setupButtons()
        observeState()
    }

    private fun setupFormatDropdowns() {
        // Default video format
        val videoFormats = VideoFormat.entries.map { it.name }
        binding.defaultVideoFormatDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, videoFormats)
        )
        binding.defaultVideoFormatDropdown.setText(VideoFormat.MP4.name, false)
        binding.defaultVideoFormatDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.setDefaultVideoFormat(VideoFormat.entries[position].name)
        }

        // Default audio format
        val audioFormats = AudioFormat.entries.map { it.name }
        binding.defaultAudioFormatDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, audioFormats)
        )
        binding.defaultAudioFormatDropdown.setText(AudioFormat.MP3.name, false)
        binding.defaultAudioFormatDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.setDefaultAudioFormat(AudioFormat.entries[position].name)
        }

        // Default image format
        val imageFormats = ImageFormat.entries.map { it.name }
        binding.defaultImageFormatDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, imageFormats)
        )
        binding.defaultImageFormatDropdown.setText(ImageFormat.JPEG.name, false)
        binding.defaultImageFormatDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.setDefaultImageFormat(ImageFormat.entries[position].name)
        }
    }

    private fun setupButtons() {
        binding.changeOutputDirButton.setOnClickListener {
            dirPickerLauncher.launch(null)
        }

        binding.outputDirText.setOnClickListener {
            viewModel.clearOutputDirectory()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    if (settings.outputDirTreeUri.isNotEmpty()) {
                        binding.outputDirText.text = settings.outputDirTreeUri
                    } else {
                        binding.outputDirText.setText(R.string.app_default_directory)
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
