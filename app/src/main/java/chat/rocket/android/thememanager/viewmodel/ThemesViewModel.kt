package chat.rocket.android.thememanager.viewmodel

import androidx.lifecycle.ViewModel
import chat.rocket.android.thememanager.model.Theme
import chat.rocket.android.thememanager.infrastructure.ThemesRepository
import javax.inject.Inject

class ThemesViewModel @Inject constructor(private val themesRepository: ThemesRepository)
    : ViewModel() {

    //no data to be retained across configuration changes yet
    fun getThemes() = themesRepository.getThemes()
    fun addTheme(theme : Theme) = themesRepository.addTheme(theme)
    fun saveTheme(theme : String) = themesRepository.saveTheme(theme)
    fun getCurrentTheme() = themesRepository.getCurrentTheme()
    fun getLeftToggle() = themesRepository.getLeftToggle()
    fun getRightToggle() = themesRepository.getRightToggle()
}
