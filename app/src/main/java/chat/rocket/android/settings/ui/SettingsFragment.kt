package chat.rocket.android.settings.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.InviteType
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.core.behaviours.AppLanguageView
import chat.rocket.android.helper.TextHelper.getDeviceAndAppInformation
import chat.rocket.android.settings.presentation.SettingsPresenter
import chat.rocket.android.settings.presentation.SettingsView
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.invalidateFirebaseToken
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.dialog_delete_account.*
import kotlinx.android.synthetic.main.fragment_settings.*
import timber.log.Timber
import javax.inject.Inject

internal const val TAG_SETTINGS_FRAGMENT = "SettingsFragment"

fun newInstance(): Fragment = SettingsFragment()

class SettingsFragment : Fragment(), SettingsView, AppLanguageView {
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var presenter: SettingsPresenter
    private val locales = arrayListOf(
        "en",
        "ar",
        "de",
        "es",
        "fa",
        "fr",
        "hi,IN",
        "it",
        "ja",
        "pt,BR",
        "pt,PT",
        "ru,RU",
        "tr",
        "uk",
        "zh,CN",
        "zh,TW"
    )

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
        setupListeners()
        setupToolbar()
        presenter.setupView()
        analyticsManager.logScreenView(ScreenViewEvent.Settings)
    }

    override fun setupSettingsView(
        avatar: String,
        displayName: String,
        status: String,
        isAdministrationEnabled: Boolean,
        isAnalyticsTrackingEnabled: Boolean,
        isDeleteAccountEnabled: Boolean,
        serverVersion: String
    ) {
        image_avatar.setImageURI(avatar)

        text_display_name.text = displayName

        text_status.text = status

        text_app_version.text =
            getString(R.string.msg_app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        text_server_version.text = getString(R.string.msg_server_version, serverVersion)

        text_administration.isVisible = isAdministrationEnabled

        with(switch_crash_report) {
            isChecked = isAnalyticsTrackingEnabled
            isEnabled = BuildConfig.FLAVOR == "play"
        }

        text_delete_account.isVisible = isDeleteAccountEnabled
    }

    override fun updateLanguage(language: String, country: String?) {
        presenter.saveLocale(language, country)
        presenter.recreateActivity()
    }

    override fun invalidateToken(token: String) = invalidateFirebaseToken(token)

    override fun showLoading() {
        view_loading.isVisible = true
    }

    override fun hideLoading() {
        view_loading.isVisible = false
    }

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    private fun setupListeners() {
        profile_container.setOnClickListener { presenter.toProfile() }

        text_contact_us.setOnClickListener { contactSupport() }

        text_language.setOnClickListener { changeLanguage() }

        text_review_this_app.setOnClickListener { showAppOnStore() }

        text_share_this_app.setOnClickListener { shareApp() }

        text_license.setOnClickListener {
            presenter.toLicense(getString(R.string.license_url), getString(R.string.title_license))
        }

        text_administration.setOnClickListener { presenter.toAdmin() }

        switch_crash_report.setOnCheckedChangeListener { _, isChecked ->
            presenter.enableAnalyticsTracking(isChecked)
        }

        text_logout.setOnClickListener { showLogoutDialog() }

        text_delete_account.setOnClickListener { showDeleteAccountDialog() }
    }

    private fun setupToolbar() {
        with((activity as AppCompatActivity)) {
            with(toolbar) {
                setSupportActionBar(this)
                title = getString(R.string.title_settings)
                setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
                setNavigationOnClickListener { activity?.onBackPressed() }
            }
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

    private fun changeLanguage() {
        context?.let {
            val selectedLocale = presenter.getCurrentLocale(it)
            var localeIndex = -1
            locales.forEachIndexed { index, locale ->
                val array = locale.split(",")
                val language = array[0]
                val country = if (array.size > 1) array[1] else ""
                // If language and country are specified, return the respective locale, else return
                // the first locale found if the language is as specified regardless of the country.
                if (language == selectedLocale.language) {
                    if (country == selectedLocale.country) {
                        localeIndex = index
                        return@forEachIndexed
                    } else if (localeIndex == -1) {
                        localeIndex = index
                    }
                }
            }
            AlertDialog.Builder(it)
                .setTitle(R.string.title_choose_language)
                .setSingleChoiceItems(
                    resources.getStringArray(R.array.languages), localeIndex
                ) { dialog, option ->
                    val array = locales[option].split(",")
                    if (array.size > 1) {
                        updateLanguage(array[0], array[1])
                    } else {
                        updateLanguage(array[0])
                    }
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private fun showAppOnStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.market_link).toUri()))
        } catch (error: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.play_store_link).toUri()))
        }
    }

    private fun shareApp() {
        // We can't know for sure at this point that the invitation was sent successfully since they will now be outside our app
        analyticsManager.logInviteSent(InviteType.ViaApp)
        presenter.shareViaApp(context)
    }

    private fun showLogoutDialog() {
        context?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.title_are_you_sure)
                .setPositiveButton(R.string.action_logout) { _, _ -> presenter.logout() }
                .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        }
    }

    private fun showDeleteAccountDialog() {
        context?.let {
            val dialogLayout = layoutInflater.inflate(R.layout.dialog_delete_account, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.text_password)

            AlertDialog.Builder(it)
                .setView(dialogLayout)
                .setPositiveButton(R.string.msg_delete_account) { _, _ ->
                    presenter.deleteAccount(editText.text.toString())
                }.setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        }
    }
}