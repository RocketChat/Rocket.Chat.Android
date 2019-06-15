package chat.rocket.android.thememanager

import chat.rocket.android.thememanager.model.ThemesRepository
import chat.rocket.android.thememanager.viewmodel.ThemesViewModelFactory

object InjectorUtils {

    fun provideThemesViewModelFactory(): ThemesViewModelFactory {
        // The whole dependency tree is constructed right here, in one place
        val themeRepository = ThemesRepository()
        return ThemesViewModelFactory(themeRepository)
    }
}