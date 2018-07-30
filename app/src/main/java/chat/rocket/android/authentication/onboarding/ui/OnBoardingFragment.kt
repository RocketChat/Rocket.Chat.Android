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
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.extensions.inflate
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import kotlinx.android.synthetic.main.fragment_authentication_on_boarding.*
import javax.inject.Inject

class OnBoardingFragment : Fragment(), OnBoardingView {
    @Inject
    lateinit var presenter: OnBoardingPresenter
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
            (activity as AuthenticationActivity).addFragmentBackStack("ServerFragment", R.id.fragment_container) {
                ServerFragment.newInstance(deepLinkInfo)
            }
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hideLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    companion object {
        fun newInstance() = OnBoardingFragment()
    }
}
