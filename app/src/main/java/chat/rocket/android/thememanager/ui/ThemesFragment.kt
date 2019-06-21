package chat.rocket.android.thememanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.thememanager.adapter.ThemesAdapter
import chat.rocket.android.thememanager.viewmodel.ThemesViewModel
import chat.rocket.android.thememanager.viewmodel.ThemesViewModelFactory
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_theme.*
import javax.inject.Inject
import chat.rocket.android.thememanager.model.Theme

internal const val TAG_THEME_FRAGMENT = "ThemesFragment"

fun newInstance() = ThemesFragment()

class ThemesFragment : Fragment() {
    @Inject
    lateinit var factory: ThemesViewModelFactory
    private lateinit var viewModel: ThemesViewModel
    private lateinit var adapter: ThemesAdapter
    var currentTheme: String = "AppTheme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this, factory).get(ThemesViewModel::class.java)
        currentTheme = viewModel.getCurrentTheme()!!
        applyTheme(activity)
        return container?.inflate(R.layout.fragment_theme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.getThemes().observe(this, Observer { themes ->
            setupRecyclerView(themes)
        })
    }

    private fun applyTheme(activity: FragmentActivity?){
        when(currentTheme){
            "AppTheme" -> activity?.setTheme(R.style.AppTheme)
            "DarkTheme" -> activity?.setTheme(R.style.DarkTheme)
            "BlackTheme" -> activity?.setTheme(R.style.BlackTheme)
        }
    }

    private fun saveTheme(theme:Theme){
        viewModel.saveTheme(theme.toString())
        reloadFragment()
    }

    private fun reloadFragment() {
        fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
    }

    private fun setupToolbar() {
        with((activity as AppCompatActivity)) {
            with(toolbar) {
                setSupportActionBar(this)
                title = getString(R.string.title_change_theme)
                setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
                setNavigationOnClickListener { activity?.onBackPressed() }
            }
        }
    }

    private fun setupRecyclerView(themes : List<Theme>) {
        ui {
            adapter = ThemesAdapter(themes, listener = {theme:Theme -> saveTheme(theme)})
            recycler_view.layoutManager = LinearLayoutManager(context)
            recycler_view.addItemDecoration(DividerItemDecoration(it, DividerItemDecoration.HORIZONTAL))
            recycler_view.adapter = adapter
        }
    }

}
