package chat.rocket.android.thememanager.infrastructure

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import chat.rocket.android.thememanager.model.Theme
import javax.inject.Inject

const val SELECTED_THEME = "selected_theme"

class ThemesRepository @Inject constructor(private val preferences: SharedPreferences){

    private val themeList = mutableListOf<Theme>()
    private val themes = MutableLiveData<List<Theme>>()

    init {
        val theme1 = Theme("AppTheme")
        val theme2 = Theme("DarkTheme")
        val theme3 = Theme("BlackTheme")
        themeList.add(theme1)
        themeList.add(theme2)
        themeList.add(theme3)
        themes.value = themeList
    }

    fun addTheme(theme : Theme) {
        themeList.add(theme)
        themes.value = themeList
    }

    fun getThemes() = themes as LiveData<List<Theme>>

    fun saveTheme(theme : String){
        with(preferences) {
            edit().putString(SELECTED_THEME, theme).apply()
        }
    }

    fun getCurrentTheme() : String? {
        return preferences.getString(SELECTED_THEME, "AppTheme")
    }
}
