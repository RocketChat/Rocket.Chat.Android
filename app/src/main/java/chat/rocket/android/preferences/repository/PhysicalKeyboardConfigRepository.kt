package chat.rocket.android.preferences.repository

import chat.rocket.android.emoji.PhysicalKeyboardConfig

interface PhysicalKeyboardConfigRepository {

    fun getConfig(): PhysicalKeyboardConfig

    fun saveConfig(physicalKeyboardConfig: PhysicalKeyboardConfig)

}
