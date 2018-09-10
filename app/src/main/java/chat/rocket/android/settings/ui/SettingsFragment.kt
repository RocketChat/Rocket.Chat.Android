package chat.rocket.android.settings.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.about.ui.AboutFragment
import chat.rocket.android.about.ui.TAG_ABOUT_FRAGMENT
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.preferences.ui.PreferencesFragment
import chat.rocket.android.preferences.ui.TAG_PREFERENCES_FRAGMENT
import chat.rocket.android.settings.password.ui.PasswordActivity
import chat.rocket.android.settings.presentation.SettingsView
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.extensions.inflate
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject
import kotlin.reflect.KClass

internal const val TAG_SETTINGS_FRAGMENT = "SettingsFragment"

class SettingsFragment : Fragment(), SettingsView, AdapterView.OnItemClickListener {
    @Inject
    lateinit var analyticsManager: AnalyticsManager

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_settings)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListView()
        analyticsManager.logScreenView(ScreenViewEvent.Settings)
    }

    override fun onResume() {
        // FIXME - gambiarra ahead. will fix when moving to new androidx Navigation
        (activity as? MainActivity)?.setupNavigationView()
        super.onResume()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.getItemAtPosition(position).toString()) {
            resources.getString(R.string.title_preferences) -> {
                (activity as AppCompatActivity).addFragmentBackStack(
                    TAG_PREFERENCES_FRAGMENT,
                    R.id.fragment_container
                ) {
                    PreferencesFragment.newInstance()
                }
            }
            resources.getString(R.string.title_change_password) ->
                startNewActivity(PasswordActivity::class)
            resources.getString(R.string.title_about) -> {
                (activity as AppCompatActivity).addFragmentBackStack(
                    TAG_ABOUT_FRAGMENT,
                    R.id.fragment_container
                ) {
                    AboutFragment.newInstance()
                }
            }
        }
    }

    private fun setupListView() {
        settings_list.onItemClickListener = this
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)?.supportActionBar?.title =
                getString(R.string.title_settings)
    }

    private fun startNewActivity(classType: KClass<out AppCompatActivity>) {
        startActivity(Intent(activity, classType.java))
        activity?.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
