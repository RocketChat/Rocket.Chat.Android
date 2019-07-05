package chat.rocket.android.thememanager.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.thememanager.BaseActivity
import chat.rocket.android.thememanager.adapter.ThemesAdapter
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.activity_themes.*
import chat.rocket.android.thememanager.model.Theme

fun newInstance() = ThemesActivity()

class ThemesActivity : BaseActivity() {
    private lateinit var adapter: ThemesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_themes)
        setupToolbar()
        setDate()
        subscribeUi()
        setupListeners()
    }

    private fun setDate(){
        theme_last_changed.text = getString(R.string.last_changed,viewModel.getSavedDate())
    }

    private fun subscribeUi() {
        viewModel.getThemes().observe(this, Observer { themes ->
            setupRecyclerView(themes)
        })
    }

    private fun saveTheme(theme:Theme){
        viewModel.saveTheme(theme.toString())
        reloadActivity()
    }

    private fun reloadActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(Intent(this, ThemesActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        } else {
            recreate()
        }
    }

    private fun setupToolbar() {
        with((this as AppCompatActivity)) {
            with(toolbar) {
                setSupportActionBar(this)
                title = getString(R.string.title_change_theme)
                setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
                setNavigationOnClickListener { onBackPressed() }
            }
        }
    }

    private fun setupRecyclerView(themes : List<Theme>) {
        adapter = ThemesAdapter(themes, listener = {theme:Theme -> saveTheme(theme)})
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
    }

    private fun setupListeners() {
        revert_to_default.setOnClickListener {
            viewModel.saveTheme("AppTheme")
            reloadActivity()
        }
    }
}
