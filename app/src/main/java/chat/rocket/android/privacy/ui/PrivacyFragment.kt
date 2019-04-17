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
import android.widget.AdapterView
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.webview.ui.webViewIntent


internal const val TAG_PRIVACY_FRAGMENT = "PrivacyFragment"

class PrivacyFragment : Fragment(), PrivacyView {
    @Inject
    lateinit var presenter: PrivacyPresenter

    private val values = arrayListOf("all", "contacts", "none")
    private val choices = arrayListOf("Everyone", "My contacts", "None")

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
        presenter.showDiscoverability()
        text_privacy_policy.setOnClickListener{
            activity?.startActivity(
                context?.webViewIntent(
                    getString(R.string.privacy_policy_url),
                    getString(R.string.title_privacy_policy)
                )
            )
        }

        text_terms_of_service.setOnClickListener {
            activity?.startActivity(
                context?.webViewIntent(
                    getString(R.string.terms_of_service_url),
                    getString(R.string.title_terms_of_service)
                )
            )
        }
    }

    override fun showDiscoverability(discoverability: String) {
        val spinner = view?.findViewById(R.id.spinner_privacy) as Spinner
        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, choices)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selection = spinner.selectedItem.toString()
                presenter.setDiscoverability(values.get(choices.indexOf(selection)));
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        spinner.setSelection(values.indexOf(discoverability))
    }

    private fun setupToolbar() {
        with((activity as MainActivity).toolbar) {
            title = getString(R.string.title_privacy)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }
    }

    companion object {
        fun newInstance() = PrivacyFragment()
    }
}
