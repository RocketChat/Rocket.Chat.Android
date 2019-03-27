package chat.rocket.android.settings.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.about.ui.AboutFragment
import chat.rocket.android.about.ui.TAG_ABOUT_FRAGMENT
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.helper.TextHelper.getDeviceAndAppInformation
import chat.rocket.android.main.presentation.MainPresenter
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.preferences.ui.PreferencesFragment
import chat.rocket.android.preferences.ui.TAG_PREFERENCES_FRAGMENT
import chat.rocket.android.settings.password.ui.PasswordActivity
import chat.rocket.android.settings.presentation.SettingsView
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.webview.ui.webViewIntent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_settings.*
import timber.log.Timber
import javax.inject.Inject

internal const val TAG_SETTINGS_FRAGMENT = "SettingsFragment"

class SettingsFragment : Fragment(), SettingsView, AdapterView.OnItemClickListener {
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var presenter: MainPresenter

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

            resources.getStringArray(R.array.settings_actions)[2] -> changeLanguage()

            resources.getStringArray(R.array.settings_actions)[3] -> shareApp()

            resources.getStringArray(R.array.settings_actions)[4] -> showAppOnStore()

            resources.getStringArray(R.array.settings_actions)[5] -> contactSupport()

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
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.title_settings)
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
        val uriText = "mailto:${"support@rocket.chat"}" +
                "?subject=" + Uri.encode(getString(R.string.msg_android_app_support)) +
                "&body=" + Uri.encode(getDeviceAndAppInformation())

        with(Intent(Intent.ACTION_SENDTO)) {
            data = uriText.toUri()
            try {
                startActivity(Intent.createChooser(this, getString(R.string.msg_send_email)))
            } catch (ex: ActivityNotFoundException) {
                Timber.e(ex)
            }
        }
    }

    fun changeLanguage() {
        val languages = resources.getStringArray(R.array.languages)

        context?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.title_choose_language)
                .setSingleChoiceItems(languages, -1) { dialog, which ->
                    when (which) {
                        0 -> {
                            presenter.setLocale("en", it)
                            activity?.recreate()
                        }
                        1 -> {
                            presenter.setLocale("hi", it)
                            activity?.recreate()
                        }
                        2 -> {
                            presenter.setLocale("ja", it)
                            activity?.recreate()
                        }
                        3 -> {
                            presenter.setLocale("ru", it)
                            activity?.recreate()
                        }
                        4 -> {
                            presenter.setLocale("it", it)
                            activity?.recreate()
                        }
                        5->{
                            presenter.setLocaleWithRegion("pt", "BR", it)
                            activity?.recreate()
                        }
                        6->{
                            presenter.setLocaleWithRegion("pt", "PT", it)
                            activity?.recreate()
                        }
                        7->{
                            presenter.setLocale("zh", it)
                            activity?.recreate()
                        }
                        8->{
                            presenter.setLocale("de", it)
                            activity?.recreate()
                        }
                        9->{
                            presenter.setLocale("es", it)
                            activity?.recreate()
                        }
                        10->{
                            presenter.setLocale("fa", it)
                            activity?.recreate()
                        }
                        11->{
                            presenter.setLocale("fr", it)
                            activity?.recreate()
                        }
                        12->{
                            presenter.setLocale("tr", it)
                            activity?.recreate()
                        }
                        13->{
                            presenter.setLocale("uk", it)
                            activity?.recreate()
                        }
                    }
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
