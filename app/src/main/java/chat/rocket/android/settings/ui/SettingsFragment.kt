package chat.rocket.android.settings.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
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

// WIDECHAT
import chat.rocket.android.helper.Constants
import kotlinx.android.synthetic.main.app_bar.* // need this for back button in setupToolbar
import kotlinx.android.synthetic.main.fragment_settings_widechat.*

internal const val TAG_SETTINGS_FRAGMENT = "SettingsFragment"

class SettingsFragment : Fragment(), SettingsView, AdapterView.OnItemClickListener {
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    // WIDECHAT
    private var settingsFragment: Int = R.layout.fragment_settings_widechat

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

       if (!Constants.WIDECHAT) {
           settingsFragment = R.layout.fragment_settings
       }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(settingsFragment)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListView()
        analyticsManager.logScreenView(ScreenViewEvent.Settings)
    }

    override fun onResume() {
        // FIXME - gambiarra ahead. will fix when moving to new androidx Navigation
        // WIDECHAT - do not recreate the nav drawer upon resume
        if (!Constants.WIDECHAT) {
            (activity as? MainActivity)?.setupNavigationView()
        }
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
            // WIDECHAT
            resources.getString(R.string.log_out) -> {
                with((activity as MainActivity).presenter) {
                    logout()
                }
            }

            resources.getString(R.string.title_share_the_app) ->{
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val shareBody = getString(R.string.msg_check_this_out)
                val shareSub = getString(R.string.play_store_link)
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareBody)
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareSub)
                startActivity(Intent.createChooser(shareIntent, getString(R.string.msg_share_using)))
            }
            resources.getString(R.string.title_rate_us) -> startAppPlayStore()
        }
    }

    private fun startAppPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.market_link).toUri()))
        } catch (error: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.play_store_link).toUri()))
        }
    }

    private fun setupListView() {
        if (Constants.WIDECHAT) {
            widechat_settings_list.onItemClickListener = this
        } else {
            settings_list.onItemClickListener = this
        }
    }

    private fun setupToolbar() {
        if (Constants.WIDECHAT){
            // WIDECHAT - added this to get the back button
            with((activity as MainActivity).toolbar) {
                title = getString(R.string.title_settings)
                setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
                setNavigationOnClickListener {
                    activity?.onBackPressed()
                }
                (activity as AppCompatActivity?)?.supportActionBar?.setDisplayShowCustomEnabled(false)
            }
        } else {
            (activity as AppCompatActivity?)?.supportActionBar?.title =
                getString(R.string.title_settings)
        }

    }

    private fun startNewActivity(classType: KClass<out AppCompatActivity>) {
        startActivity(Intent(activity, classType.java))
        activity?.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
