package chat.rocket.android.settings.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.about.ui.AboutFragment
import chat.rocket.android.about.ui.TAG_ABOUT_FRAGMENT
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.helper.TextHelper.getDeviceAndAppInformation
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.preferences.ui.PreferencesFragment
import chat.rocket.android.preferences.ui.TAG_PREFERENCES_FRAGMENT
import chat.rocket.android.settings.password.ui.PasswordActivity
import chat.rocket.android.settings.presentation.SettingsView
import chat.rocket.android.settings.presentation.settingPresenter
import chat.rocket.android.util.extensions.*
import chat.rocket.android.webview.ui.webViewIntent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_download.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import timber.log.Timber
import javax.inject.Inject

internal const val TAG_SETTINGS_FRAGMENT = "SettingsFragment"

class SettingsFragment : Fragment(), SettingsView, AdapterView.OnItemClickListener {

    @Inject
    lateinit var presenter: settingPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
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
            resources.getStringArray(R.array.settings_actions)[0] -> {
                (activity as AppCompatActivity).addFragmentBackStack(
                        TAG_PREFERENCES_FRAGMENT,
                        R.id.fragment_container
                ) {
                    PreferencesFragment.newInstance()
                }
            }

            resources.getStringArray(R.array.settings_actions)[1] ->
                activity?.startActivity(Intent(activity, PasswordActivity::class.java))

            resources.getStringArray(R.array.settings_actions)[2] -> shareApp()

            resources.getStringArray(R.array.settings_actions)[3] -> showAppOnStore()

            resources.getStringArray(R.array.settings_actions)[4] -> contactSupport()

            resources.getStringArray(R.array.settings_actions)[5] -> presenter.downloadData();

            resources.getStringArray(R.array.settings_actions)[6] -> activity?.startActivity(
                context?.webViewIntent(
                    getString(R.string.license_url),
                    getString(R.string.title_licence)
                )
            )

            resources.getStringArray(R.array.settings_actions)[7] -> {
                (activity as AppCompatActivity).addFragmentBackStack(
                        TAG_ABOUT_FRAGMENT,
                        R.id.fragment_container
                ) {
                    AboutFragment.newInstance()
                }
            }
     

        }
    }

    private fun showAppOnStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.market_link).toUri()))
        } catch (error: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.play_store_link).toUri()))
        }
    }

    private fun setupListView() {
        settings_list.onItemClickListener = this
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)?.supportActionBar?.title =
                getString(R.string.title_settings)
    }

    private fun shareApp() {
        with(Intent(Intent.ACTION_SEND)) {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msg_check_this_out))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.play_store_link))
            startActivity(Intent.createChooser(this, getString(R.string.msg_share_using)))
        }
    }

    private fun contactSupport() {
        with(Intent(Intent.ACTION_SEND)) {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@rocket.chat"))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msg_android_app_support))
            putExtra(Intent.EXTRA_TEXT, getDeviceAndAppInformation())
            try {
                startActivity(Intent.createChooser(this, getString(R.string.msg_send_email)))
            } catch (ex: ActivityNotFoundException) {
                Timber.e(ex)
            }
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }



    override fun showMessage(resId: Int) {
        ui { val builder = AlertDialog.Builder(activity)
            val view = layoutInflater.inflate(R.layout.dialog_download, null)
            view.text_download_description.textContent = resources.getString(resId);
            builder.setView(view)
            builder.setPositiveButton(R.string.msg_ok) { dialog, _ ->
                dialog.cancel()
            }
            builder.show() }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))


    override fun showLoading() {
        ui {
            if (view_setting_loading != null) {
                view_setting_loading.isVisible = true
            }
        }
    }

    override fun hideLoading() {
        ui {
            if (view_setting_loading != null) {
                view_setting_loading.isVisible = false
            }
        }
    }
}
