package chat.rocket.android.thememanager

import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import chat.rocket.android.R
import chat.rocket.android.thememanager.util.ThemeUtil
import chat.rocket.android.thememanager.viewmodel.ThemesViewModel
import chat.rocket.android.thememanager.viewmodel.ThemesViewModelFactory
import dagger.android.AndroidInjection
import javax.inject.Inject

open class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var factory: ThemesViewModelFactory
    lateinit var viewModel: ThemesViewModel
    private var currentTheme: String = "AppTheme"

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(ThemesViewModel::class.java)
        currentTheme = viewModel.getCurrentTheme()!!
        applyTheme(currentTheme)
        ThemeUtil.setTheme(theme)
    }

    override fun onResume() {
        super.onResume()
        val selectedTheme = viewModel.getCurrentTheme()!!
        if(currentTheme != selectedTheme)
            recreate()
    }

    private fun applyTheme(currentTheme: String) {
        when(currentTheme){
            "AppTheme" -> setTheme(R.style.AppTheme)
            "DarkTheme" -> setTheme(R.style.DarkTheme)
            "BlackTheme" -> setTheme(R.style.BlackTheme)
        }
    }
}