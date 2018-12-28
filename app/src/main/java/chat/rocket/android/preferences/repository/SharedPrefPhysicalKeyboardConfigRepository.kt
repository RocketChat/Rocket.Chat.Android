package chat.rocket.android.preferences.repository

import android.content.SharedPreferences
import chat.rocket.android.emoji.PhysicalKeyboardConfig
import javax.inject.Inject

class SharedPrefPhysicalKeyboardConfigRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : PhysicalKeyboardConfigRepository {

    override fun saveConfig(physicalKeyboardConfig: PhysicalKeyboardConfig) {
        sharedPreferences.edit()
            .putInt(PHYSICAL_KEYBOARD_CONFIG_KEY, physicalKeyboardConfig.state)
            .apply()
    }

    override fun getConfig(): PhysicalKeyboardConfig {
        return PhysicalKeyboardConfig.Disable.stateToConfig(
            sharedPreferences.getInt(
                PHYSICAL_KEYBOARD_CONFIG_KEY,
                PhysicalKeyboardConfig.EnterPlusControl.state
            )
        )
    }

    companion object {
        const val PHYSICAL_KEYBOARD_CONFIG_KEY = "PHYSICAL_KEYBOARD_CONFIG_KEY"
    }

}
