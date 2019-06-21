package chat.rocket.android.thememanager.viewmodel

import androidx.lifecycle.ViewModel
import chat.rocket.android.thememanager.model.Theme
import chat.rocket.android.thememanager.infrastructure.ThemesRepository
import javax.inject.Inject

class ThemesViewModel @Inject constructor(private val themesRepository: ThemesRepository)
    : ViewModel() {

    fun getThemes() = themesRepository.getThemes()
    fun addTheme(theme : Theme) = themesRepository.addTheme(theme)
    fun saveTheme(theme : String) = themesRepository.saveTheme(theme)
    fun getCurrentTheme() = themesRepository.getCurrentTheme()
}
