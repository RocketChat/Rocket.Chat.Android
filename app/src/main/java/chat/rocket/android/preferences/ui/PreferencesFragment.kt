package chat.rocket.android.preferences.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.preferences.presentation.PreferencesPresenter
import chat.rocket.android.preferences.presentation.PreferencesView
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_preferences.*
import javax.inject.Inject

internal const val TAG_PREFERENCES_FRAGMENT = "PreferencesFragment"

class PreferencesFragment : Fragment(), PreferencesView {
    @Inject
    lateinit var presenter: PreferencesPresenter
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
    ): View? = inflater.inflate(R.layout.fragment_preferences, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListeners()
        presenter.loadAnalyticsTrackingInformation()

        analyticsManager.logScreenView(ScreenViewEvent.Preferences)
    }

    override fun setupAnalyticsTrackingView(isAnalyticsTrackingEnabled: Boolean) {
        if (BuildConfig.FLAVOR == "foss") {
            switch_analytics_tracking.isChecked = false
            switch_analytics_tracking.isEnabled = false
            text_analytics_tracking_description.text =
                    getString(R.string.msg_not_applicable_since_it_is_a_foss_version)
            return
        }

        if (isAnalyticsTrackingEnabled) {
            text_analytics_tracking_description.text =
                    getString(R.string.msg_send_analytics_tracking)
        } else {
            text_analytics_tracking_description.text =
                    getString(R.string.msg_do_not_send_analytics_tracking)
        }
        switch_analytics_tracking.isChecked = isAnalyticsTrackingEnabled
    }

    private fun setupToolbar() {
        with((activity as MainActivity).toolbar) {
            title = getString(R.string.title_preferences)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }
    }

    private fun setupListeners() {
        switch_analytics_tracking.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                text_analytics_tracking_description.text =
                        getString(R.string.msg_send_analytics_tracking)
                presenter.enableAnalyticsTracking()
            } else {
                text_analytics_tracking_description.text =
                        getString(R.string.msg_do_not_send_analytics_tracking)
                presenter.disableAnalyticsTracking()
            }
        }
    }

    companion object {
        fun newInstance() = PreferencesFragment()
    }
}
