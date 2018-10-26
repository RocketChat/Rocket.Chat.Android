package chat.rocket.android.authentication.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.onboarding.presentation.OnBoardingPresenter
import chat.rocket.android.authentication.onboarding.presentation.OnBoardingView
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setLightStatusBar
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_authentication_on_boarding.*
import javax.inject.Inject

// WIDECHAT
import chat.rocket.android.helper.Constants
import kotlinx.android.synthetic.main.fragment_authentication_widechat_on_boarding.*

fun newInstance() = OnBoardingFragment()

class OnBoardingFragment : Fragment(), OnBoardingView {
    @Inject
    lateinit var presenter: OnBoardingPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    // WIDECHAT - replace the orignal RC onboarding screen with a blank loading screen while we by default sign in to our default server
    private var auth_fragment: Int = R.layout.fragment_authentication_widechat_on_boarding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        if (!Constants.WIDECHAT) {
            auth_fragment = R.layout.fragment_authentication_on_boarding
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(auth_fragment)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        if (Constants.WIDECHAT) {
            // WIDECHAT - this is where we bypass the original RC onboarding sequence,
            // sign on to our server, and provide the login buttons inside LoginOptionsFragment.kt
            joinWidechatServer()
        } else {
            setupOnClickListener()
        }
        analyticsManager.logScreenView(ScreenViewEvent.OnBoarding)
    }

    private fun setupToolbar() {
        with(activity as AuthenticationActivity) {
            if (!Constants.WIDECHAT) {
                view?.let { this.setLightStatusBar(it) }
            }
            toolbar.isVisible = false
        }
    }

    private fun setupOnClickListener() {
        connect_with_a_server_container.setOnClickListener { signInToYourServer() }
        join_community_container.setOnClickListener { joinInTheCommunity() }
        create_server_container.setOnClickListener { createANewServer() }
    }

    override fun showLoading() {
        ui {
            if (Constants.WIDECHAT) {
                widechat_view_loading.isVisible = true
            } else {
                view_loading.isVisible = true
            }
        }
    }

    override fun hideLoading() {
        ui {
            if (Constants.WIDECHAT) {
                widechat_view_loading.isVisible = false
            } else {
                view_loading.isVisible = false
            }
        }
    }

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

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    private fun signInToYourServer() = ui {
        presenter.toSignInToYourServer()
    }

    private fun joinWidechatServer() = ui {
        presenter.connectToCommunityServer(
                getString(R.string.default_protocol) + getString(R.string.widechat_server_url)
        )
    }

    private fun joinInTheCommunity() = ui {
        presenter.connectToCommunityServer(
            getString(R.string.default_protocol) + getString(R.string.community_server_url)
        )
    }

    private fun createANewServer() = ui {
        presenter.toCreateANewServer(
            getString(R.string.default_protocol) + getString(R.string.create_server_url)
        )
    }
}
