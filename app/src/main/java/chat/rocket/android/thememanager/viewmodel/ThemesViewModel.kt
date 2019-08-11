package chat.rocket.android.thememanager.viewmodel

import androidx.lifecycle.ViewModel
import chat.rocket.android.thememanager.infrastructure.ThemesRepository
import javax.inject.Inject

class ThemesViewModel @Inject constructor(private val themesRepository: ThemesRepository)
    : ViewModel() {

    fun getThemes() = themesRepository.getThemes()
    fun getThemeNames() = themesRepository.getThemeNames()
    fun saveTheme(theme: String) = themesRepository.saveTheme(theme)
    fun getCurrentTheme() = themesRepository.getCurrentTheme()
    fun addCustomTheme(baseThemeIndex: Int, name: String) = themesRepository.addCustomTheme(baseThemeIndex, name)
    fun saveCustomTheme(themeIndex: Int, baseThemeName: String) = themesRepository.saveCustomTheme(themeIndex, baseThemeName)
    fun getCustomThemes() = themesRepository.getCustomThemes()
    fun getSavedDate() = themesRepository.getSavedDate()
    fun removeCustomTheme(position: Int) = themesRepository.removeCustomTheme(position)
    fun editCustomTheme(colorType: String, themeIndex: Int, color: Int) = themesRepository.editCustomTheme(colorType, themeIndex, color)
    fun getCurrentThemeName() = themesRepository.getCurrentThemeName()
    fun getIsCustom() = themesRepository.getIsCustom()
}
