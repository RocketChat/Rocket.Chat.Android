package chat.rocket.android.preferences.interactor

import chat.rocket.android.emoji.PhysicalKeyboardConfig
import chat.rocket.android.preferences.repository.PhysicalKeyboardConfigRepository
import javax.inject.Inject

class PhysicalKeyboardConfigInteractor @Inject constructor(
    private val physicalKeyboardConfigRepository: PhysicalKeyboardConfigRepository
) {

    fun getConfig() = physicalKeyboardConfigRepository.getConfig()

    fun saveConfig(physicalKeyboardConfig: PhysicalKeyboardConfig) {
        physicalKeyboardConfigRepository.saveConfig(physicalKeyboardConfig)
    }

}
