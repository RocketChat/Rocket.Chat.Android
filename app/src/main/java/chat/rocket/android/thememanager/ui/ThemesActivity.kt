package chat.rocket.android.thememanager.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.thememanager.BaseActivity
import chat.rocket.android.thememanager.adapter.ThemesAdapter
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_theme.*
import chat.rocket.android.thememanager.model.Theme

fun newInstance() = ThemesActivity()

class ThemesActivity : BaseActivity() {
    private lateinit var adapter: ThemesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_theme)
        setupToolbar()
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.getThemes().observe(this, Observer { themes ->
            setupRecyclerView(themes)
        })
    }

    private fun saveTheme(theme:Theme){
        viewModel.saveTheme(theme.toString())
        reloadFragment()
    }

    private fun reloadFragment() {
        recreate()
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
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL))
        recycler_view.adapter = adapter
    }
}
