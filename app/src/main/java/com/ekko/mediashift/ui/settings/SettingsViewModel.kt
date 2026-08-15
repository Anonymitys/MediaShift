package com.ekko.mediashift.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.ekko.mediashift.data.AppSettings
import com.ekko.mediashift.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    private val _settings = MutableStateFlow(repository.getSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun reloadSettings() {
        _settings.value = repository.getSettings()
    }

    fun setOutputDirectory(uri: Uri) {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        getApplication<Application>().contentResolver.takePersistableUriPermission(uri, flags)
        repository.setOutputDirUri(uri.toString())
        reloadSettings()
    }

    fun clearOutputDirectory() {
        repository.clearOutputDirectory()
        reloadSettings()
    }

    fun setDefaultVideoFormat(format: String) {
        repository.setDefaultVideoFormat(format)
        reloadSettings()
    }

    fun setDefaultAudioFormat(format: String) {
        repository.setDefaultAudioFormat(format)
        reloadSettings()
    }

    fun setDefaultImageFormat(format: String) {
        repository.setDefaultImageFormat(format)
        reloadSettings()
    }
}
