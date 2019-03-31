package chat.rocket.android.privacy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.privacy.presentation.PrivacyView
import chat.rocket.android.privacy.presentation.PrivacyPresenter
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_privacy.*
import javax.inject.Inject
import android.widget.ArrayAdapter



internal const val TAG_PRIVACY_FRAGMENT = "PrivacyFragment"

class PrivacyFragment : Fragment(), PrivacyView {
    @Inject
    lateinit var presenter: PrivacyPresenter
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
    ): View? = inflater.inflate(R.layout.fragment_privacy, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListeners()

        val spinner = view.findViewById(R.id.spinner_privacy) as Spinner
        val values = arrayListOf("Everyone", "Contacts", "None")
        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
    }

    private fun setupToolbar() {
        with((activity as MainActivity).toolbar) {
            title = getString(R.string.title_privacy)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }
    }

    private fun setupListeners() {
    }

    companion object {
        fun newInstance() = PrivacyFragment()
    }
}
