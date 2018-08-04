package chat.rocket.android.authentication.onboarding.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.authentication.domain.model.getLoginDeepLinkInfo
import chat.rocket.android.authentication.onboarding.presentation.OnBoardingPresenter
import chat.rocket.android.authentication.onboarding.presentation.OnBoardingView
import chat.rocket.android.authentication.server.ui.ServerFragment
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.*
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import kotlinx.android.synthetic.main.fragment_authentication_on_boarding.*
import javax.inject.Inject

class OnBoardingFragment : Fragment(), OnBoardingView {

    @Inject
    lateinit var presenter: OnBoardingPresenter
    private var protocol = "https://"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_authentication_on_boarding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOnClickListener()
        setupToobar()
    }

    private fun setupToobar() {
        val toolbar = (activity as AuthenticationActivity).toolbar
        toolbar.isVisible = false
    }

    private fun setupOnClickListener() {
        val deepLinkInfo = activity?.intent?.getLoginDeepLinkInfo()
        button_connect_server.setOnClickListener {
            (activity as AuthenticationActivity).addFragmentBackStack("ServerFragment", R.id.fragment_container) {
                ServerFragment.newInstance(deepLinkInfo)
            }
        }
        button_join_community.setOnClickListener {
            connectToCommunityServer()
        }
        button_create_server.setOnClickListener {
            presenter.createServer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun showLoading() {
        ui{
            view_loading.setVisible(true)
        }
    }

    override fun hideLoading() {
        ui{
            view_loading.setVisible(false)
        }
    }

    override fun showInvalidServerUrlMessage() = showMessage(getString(R.string.msg_invalid_server_url))

    override fun showMessage(resId: Int) {
        ui {
            showToast(resId)
        }
    }

    override fun showMessage(message: String) {
        ui {
            showToast(message)
        }
    }

    override fun showGenericErrorMessage() {
        showMessage(getString(R.string.msg_generic_error))
    }

    private fun connectToCommunityServer() {
        ui {
            val url = getString(R.string.community_server)
            presenter.connect("$protocol${url.sanitize()}")
        }
    }
    companion object {
        fun newInstance() = OnBoardingFragment()
    }
}
