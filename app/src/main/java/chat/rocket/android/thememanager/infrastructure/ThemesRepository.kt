package chat.rocket.android.thememanager.infrastructure

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import chat.rocket.android.thememanager.model.Theme
import javax.inject.Inject

const val SELECTED_THEME = "selected_theme"
const val DARK_THEME= "dark_theme"
const val BLACK_THEME = "black_theme"

class ThemesRepository @Inject constructor(private val preferences: SharedPreferences){

    private val themeList = mutableListOf<Theme>()
    private val themes = MutableLiveData<List<Theme>>()

    init {
        val theme1 = Theme(1, "AppTheme")
        val theme2 = Theme(2, "DarkTheme")
        val theme3 = Theme(3, "BlackTheme")
        themeList.add(theme1)
        themeList.add(theme2)
        themeList.add(theme3)
        themes.value = themeList
    }

    //not required for now
    fun addTheme(theme : Theme) {
        themeList.add(theme)
        themes.value = themeList
    }

    fun getThemes() = themes as LiveData<List<Theme>>

    fun saveTheme(theme : String){
        with(preferences) {
            edit().putString(SELECTED_THEME, theme).apply()
            if (theme.equals("DarkTheme")){
                edit().putBoolean(DARK_THEME, true).apply()
                edit().putBoolean(BLACK_THEME, false).apply()
                println("Putting DarkTheme"+theme)
            } else if (theme.equals("BlackTheme")){
                edit().putBoolean(DARK_THEME, false).apply()
                edit().putBoolean(BLACK_THEME, true).apply()
                println("Putting BlackTheme"+theme)
            }
            else if (theme.equals("AppTheme")){
                edit().putBoolean(DARK_THEME, false).apply()
                edit().putBoolean(BLACK_THEME, false).apply()
                println("Putting AppTheme"+theme)
            }
        }
    }

    fun getCurrentTheme() : String? {
        return preferences.getString(SELECTED_THEME, "AppTheme")
    }

    fun getLeftToggle() : Boolean? {
        return preferences.getBoolean(DARK_THEME, false)
    }

    fun getRightToggle() : Boolean? {
        return preferences.getBoolean(BLACK_THEME, false)
    }
}
