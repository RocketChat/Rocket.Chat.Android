package chat.rocket.android.thememanager.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ThemesRepository{

    private val themeList = mutableListOf<Theme>()
    private val themes = MutableLiveData<List<Theme>>()

    init {
        val theme1 = Theme(1,"AppTheme")
        val theme2 = Theme(2,"AppDarkTheme")
        val theme3 = Theme(3,"LibraryTheme")
        themeList.add(theme1)
        themeList.add(theme2)
        themeList.add(theme3)
        themes.value = themeList
    }

    //not required for now
    fun addTheme(theme: Theme) {
        themeList.add(theme)
        themes.value = themeList
    }

    fun getThemes() = themes as LiveData<List<Theme>>
}
