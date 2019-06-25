package chat.rocket.android.thememanager.infrastructure

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import chat.rocket.android.R
import chat.rocket.android.thememanager.model.Theme
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*


const val SELECTED_THEME = "selected_theme"
const val LAST_CHANGED = "last_changed"

class ThemesRepository @Inject constructor(private val preferences: SharedPreferences){

    private val themeList = mutableListOf<Theme>()
    private val themes = MutableLiveData<List<Theme>>()
    private val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy")
    private lateinit var currentDate : Date

    init {
        val theme1 = Theme("AppTheme", R.array.AppThemePreview)
        val theme2 = Theme("DarkTheme", R.array.DarkThemePreview)
        val theme3 = Theme("BlackTheme", R.array.BlackThemePreview)
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
            saveDate()
        }
    }

    fun saveDate(){
        with(preferences) {
            val currentTimeMillis = System.currentTimeMillis()
            edit().putLong(LAST_CHANGED, currentTimeMillis).apply()
        }
    }

    fun getSavedDate() : String {
        if(preferences.contains(LAST_CHANGED)){
            currentDate = Date(preferences.getLong(LAST_CHANGED,0))
            return simpleDateFormat.format(currentDate)
        }
        return ""
    }

    fun getCurrentTheme() : String? {
        return preferences.getString(SELECTED_THEME, "AppTheme")
    }
}
