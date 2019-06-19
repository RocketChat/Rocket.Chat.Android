package chat.rocket.android.thememanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import chat.rocket.android.thememanager.infrastructure.ThemesRepository
import javax.inject.Inject

class ThemesViewModelFactory @Inject constructor(
        private val repository: ThemesRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ThemesViewModel(repository) as T
    }
}