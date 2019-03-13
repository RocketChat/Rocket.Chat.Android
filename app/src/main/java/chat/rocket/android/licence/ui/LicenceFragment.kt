package chat.rocket.android.licence.ui

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.main.ui.MainActivity
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.toolbar
import kotlinx.android.synthetic.main.fragment_licence.text_licence
import javax.inject.Inject

internal const val TAG_LICENCE_FRAGMENT = "LicenceFragment"

class LicenceFragment : Fragment() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_licence, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        analyticsManager.logScreenView(ScreenViewEvent.About)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setUpViews() {
        text_licence.text = getHtmlFormtText(getString(R.string.rocket_chat_licence))
    }

    private fun setupToolbar() {
        with((activity as MainActivity).toolbar) {
            title = getString(R.string.title_licence)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }
    }

    @Suppress("DEPRECATION")
    private fun getHtmlFormtText(text: String) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            else Html.fromHtml(text)

    companion object {
        fun newInstance() = LicenceFragment()
    }
}
