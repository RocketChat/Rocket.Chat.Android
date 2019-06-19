package chat.rocket.android.thememanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import chat.rocket.android.R
import chat.rocket.android.thememanager.viewmodel.ThemesViewModel
import chat.rocket.android.thememanager.viewmodel.ThemesViewModelFactory
import javax.inject.Inject

open class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: ThemesViewModelFactory
    private lateinit var viewModel: ThemesViewModel
    private var currentTheme:String = "AppTheme"
//    private lateinit var TEMP_ARRAY : IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(ThemesViewModel::class.java)
        currentTheme=viewModel.getCurrentTheme()!!
        applyTheme(currentTheme)
    }

    override fun onResume() {
        super.onResume()
        val selectedTheme = viewModel.getCurrentTheme()!!
        if(currentTheme != selectedTheme)
            recreate()
    }

    private fun applyTheme(currentTheme: String) {
        if (currentTheme == "AppTheme"){
            setTheme(R.style.AppTheme)
        }
        else if(currentTheme == "DarkTheme"){
            setTheme(R.style.DarkTheme)
        }
        else if(currentTheme == "BlackTheme") {
            setTheme(R.style.BlackTheme)
        }
    }

//    fun getThemeAttributeColor(attr : Int){
//        TEMP_ARRAY[0] = attr
//        val color = getThemeAttributeColor(attr)
//        return color
//    }
}