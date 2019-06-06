package chat.rocket.android.thememanager.viewmodel

import androidx.lifecycle.ViewModel
import chat.rocket.android.thememanager.model.Theme
import chat.rocket.android.thememanager.model.ThemesRepository

class ThemesViewModel (private val themeRepository: ThemesRepository)
    : ViewModel() {

    fun getThemes() = themeRepository.getThemes()
    fun addTheme(theme: Theme) = themeRepository.addTheme(theme)
}
