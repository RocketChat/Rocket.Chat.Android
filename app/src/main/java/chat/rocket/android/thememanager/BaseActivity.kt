package chat.rocket.android.thememanager

import android.os.Bundle
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
    private var currentAccentStyle = 0
    private var currentToolbarStyle = 0
    private var currentBackgroundStyle = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(ThemesViewModel::class.java)
        currentTheme = viewModel.getCurrentTheme().name
        currentAccentStyle = viewModel.getCurrentTheme().getCustomAccentStyle(resources, this.packageName)
        currentToolbarStyle = viewModel.getCurrentTheme().getCustomToolbarStyle(resources, this.packageName)
        currentBackgroundStyle = viewModel.getCurrentTheme().getCustomBackgroundStyle(resources, this.packageName)
        applyTheme(currentTheme, currentAccentStyle, currentToolbarStyle, currentBackgroundStyle)
        ThemeUtil.setTheme(theme, viewModel.getCurrentTheme())
    }

    override fun onResume() {
        super.onResume()
        val selectedTheme = viewModel.getCurrentTheme().name
        val selectedAccentStyle = viewModel.getCurrentTheme().getCustomAccentStyle(resources, this.packageName)
        val selectedToolbarStyle = viewModel.getCurrentTheme().getCustomToolbarStyle(resources, this.packageName)
        val selectedBackgroundStyle = viewModel.getCurrentTheme().getCustomBackgroundStyle(resources, this.packageName)
        if ((currentTheme != selectedTheme)
                or (currentAccentStyle != selectedAccentStyle)
                or (currentToolbarStyle != selectedToolbarStyle)
                or (currentBackgroundStyle != selectedBackgroundStyle))
            recreate()
    }

    private fun applyTheme(currentTheme: String, currentAccentStyle: Int, currentToolbarStyle: Int, currentBackgroundStyle: Int) {
        setTheme(resources.getIdentifier(currentTheme, "style", this.packageName))
        if (currentBackgroundStyle != 0) {
            setTheme(currentBackgroundStyle)
        }
        if (currentAccentStyle != 0) {
            theme.applyStyle(currentAccentStyle, true)
        }
        if (currentToolbarStyle != 0) {
            theme.applyStyle(currentToolbarStyle, true)
        }
    }
}