package chat.rocket.android.thememanager.infrastructure

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import chat.rocket.android.R
import chat.rocket.android.thememanager.model.Theme
import com.google.gson.Gson
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val SELECTED_THEME = "selected_theme"
const val LAST_CHANGED = "last_changed"
const val IS_CUSTOM = "is_custom"
const val CUSTOM_THEMES = "custom_themes"

class ThemesRepository @Inject constructor(private val preferences: SharedPreferences) {

    private val themeList = mutableListOf<Theme>()
    private val themes = MutableLiveData<List<Theme>>()

    private val customThemeList = mutableListOf<Theme>()
    private val customThemes = MutableLiveData<List<Theme>>()
    private val customThemeNamesArray = arrayListOf<String>()

    private var storedList = mutableListOf<Map<String, Any>>()
    private val themeNamesArray = arrayListOf<String>()

    private val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private lateinit var currentDate: Date

    init {
        val theme1 = Theme("AppTheme", R.array.AppThemePreview, false)
        val theme2 = Theme("DarkTheme", R.array.DarkThemePreview, true)
        val theme3 = Theme("BlackTheme", R.array.BlackThemePreview, true)
        themeList.add(theme1)
        themeList.add(theme2)
        themeList.add(theme3)
        val iterator = themeList.listIterator()
        for (theme in iterator) {
            themeNamesArray.add(theme.name)
        }
        themes.value = themeList
    }

    fun getThemes() = themes as LiveData<List<Theme>>

    fun saveTheme(theme: String) {
        setIsCustom(false)
        with(preferences) {
            edit().putString(SELECTED_THEME, theme).apply()
            saveDate()
        }
    }

    fun getCurrentTheme(): Theme {
        val theme: Theme
        if (getIsCustom()) {
            val storedCustomTheme = Gson().fromJson(preferences.getString(SELECTED_THEME, ""), mutableMapOf<String, Any>()::class.java)
            val baseThemeIndex = (storedCustomTheme["BaseThemeIndex"] as Double).toInt()
            val customTheme = themeList[baseThemeIndex].copy()
            val customAccent: Int = (storedCustomTheme["CustomColorAccent"] as Double).toInt()
            val customToolbar: Int = (storedCustomTheme["CustomColorToolbar"] as Double).toInt()
            val customBackground: Int = (storedCustomTheme["CustomColorBackground"] as Double).toInt()
            customTheme.customName = storedCustomTheme["ThemeName"] as String
            customTheme.customAccent = customAccent
            customTheme.customToolbar = customToolbar
            customTheme.customBackground = customBackground
            theme = customTheme
        } else {
            val storedTheme = preferences.getString(SELECTED_THEME, "AppTheme")
            val themeIndex = themeNamesArray.indexOf(storedTheme)
            theme = themeList[themeIndex]
        }
        return theme
    }

    fun getCurrentThemeName(): String {
        val name: String
        if (getIsCustom()) {
            name = getCurrentTheme().customName
        } else {
            name = getCurrentTheme().name
        }
        return name
    }

    fun setIsCustom(boolean: Boolean) {
        preferences.edit().putBoolean(IS_CUSTOM, boolean).apply()
    }

    fun getIsCustom(): Boolean {
        return preferences.getBoolean(IS_CUSTOM, false)
    }

    fun saveCustomTheme(themeIndex: Int, baseThemeName: String) {
        val baseThemeIndex = themeNamesArray.indexOf(baseThemeName)
        setIsCustom(true)
        val mapCustom = mapOf(
                "BaseTheme" to customThemeList[themeIndex].name,
                "BaseThemeIndex" to baseThemeIndex,
                "ThemeName" to customThemeList[themeIndex].customName,
                "CustomColorAccent" to customThemeList[themeIndex].customAccent,
                "CustomColorToolbar" to customThemeList[themeIndex].customToolbar,
                "CustomColorBackground" to customThemeList[themeIndex].customBackground)
        preferences.edit().putString(SELECTED_THEME, Gson().toJson(mapCustom)).apply()
    }

    fun addCustomTheme(baseThemeIndex: Int, customName: String): Boolean {
        if (customName in customThemeNamesArray) {
            return false
        } else {
            val customTheme = themeList[baseThemeIndex]
            val mapCustom = mapOf(
                    "BaseTheme" to customTheme.name,
                    "BaseThemeIndex" to baseThemeIndex,
                    "ThemeName" to customName,
                    "CustomColorAccent" to 0,
                    "CustomColorToolbar" to 0,
                    "CustomColorBackground" to 0)
            storedList.add(mapCustom)
            preferences.edit().putString(CUSTOM_THEMES, Gson().toJson(storedList)).apply()
            getCustomThemes()
        }
        return true
    }

    fun editCustomTheme(colorType: String, themeIndex: Int, color: Int): Boolean {
        var isCurrent = false
        val baseTheme = customThemeList[themeIndex].name
        var mapCustom = mapOf<String, Any>()
        when (colorType) {
            "Custom Accent" -> mapCustom = mapOf(
                    "BaseTheme" to baseTheme,
                    "BaseThemeIndex" to themeNamesArray.indexOf(baseTheme),
                    "ThemeName" to customThemeList[themeIndex].customName,
                    "CustomColorAccent" to color,
                    "CustomColorToolbar" to customThemeList[themeIndex].customToolbar,
                    "CustomColorBackground" to customThemeList[themeIndex].customBackground)
            "Custom Toolbar" -> mapCustom = mapOf(
                    "BaseTheme" to baseTheme,
                    "BaseThemeIndex" to themeNamesArray.indexOf(baseTheme),
                    "ThemeName" to customThemeList[themeIndex].customName,
                    "CustomColorAccent" to customThemeList[themeIndex].customAccent,
                    "CustomColorToolbar" to color,
                    "CustomColorBackground" to customThemeList[themeIndex].customBackground)
            "Custom Background" -> mapCustom = mapOf(
                    "BaseTheme" to baseTheme,
                    "BaseThemeIndex" to themeNamesArray.indexOf(baseTheme),
                    "ThemeName" to customThemeList[themeIndex].customName,
                    "CustomColorAccent" to customThemeList[themeIndex].customAccent,
                    "CustomColorToolbar" to customThemeList[themeIndex].customToolbar,
                    "CustomColorBackground" to color)
        }
        storedList[themeIndex] = mapCustom
        preferences.edit().putString(CUSTOM_THEMES, Gson().toJson(storedList)).apply()
        if (getCurrentThemeName() == customThemeList[themeIndex].customName) {
            saveCustomTheme(themeIndex, customThemeList[themeIndex].name)
            isCurrent = true
        }
        getCustomThemes()
        return isCurrent
    }

    fun getCustomThemes(): LiveData<List<Theme>> {
        if (preferences.contains(CUSTOM_THEMES)) {
            storedList = Gson().fromJson(preferences.getString(CUSTOM_THEMES, ""), mutableListOf<Map<String, Any>>()::class.java)
            customThemeList.clear()
            for (i in 0 until storedList.size) {
                val baseThemeIndex = storedList[i]["BaseThemeIndex"] as Double
                val customTheme = themeList[baseThemeIndex.toInt()].copy()
                val customAccent: Int = (storedList[i]["CustomColorAccent"] as Double).toInt()
                val customToolbar: Int = (storedList[i]["CustomColorToolbar"] as Double).toInt()
                val customBackground: Int = (storedList[i]["CustomColorBackground"] as Double).toInt()
                customTheme.customName = storedList[i]["ThemeName"] as String
                customTheme.customAccent = customAccent
                customTheme.customToolbar = customToolbar
                customTheme.customBackground = customBackground

                customThemeNamesArray.add(customTheme.customName)
                customThemeList.add(customTheme)
            }
            customThemes.value = customThemeList
        }
        return customThemes
    }

    fun removeCustomTheme(position: Int): Boolean {
        val customThemeName = customThemeList[position].customName
        storedList.removeAt(position)
        preferences.edit().putString(CUSTOM_THEMES, Gson().toJson(storedList)).apply()
        getCustomThemes()
        if (customThemeName == getCurrentThemeName()) {
            saveTheme("AppTheme")
            setIsCustom(false)
            return true
        }
        return false
    }

    fun saveDate() {
        with(preferences) {
            val currentTimeMillis = System.currentTimeMillis()
            edit().putLong(LAST_CHANGED, currentTimeMillis).apply()
        }
    }

    fun getSavedDate(): String {
        if (preferences.contains(LAST_CHANGED)) {
            currentDate = Date(preferences.getLong(LAST_CHANGED, 0))
            return simpleDateFormat.format(currentDate)
        }
        return ""
    }

    fun getThemeNames(): ArrayList<String> {
        return themeNamesArray
    }
}
