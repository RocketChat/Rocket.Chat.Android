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

fun newInstance() = OnBoardingFragment()

class OnBoardingFragment : Fragment(), OnBoardingView {
    @Inject
    lateinit var presenter: OnBoardingPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_authentication_on_boarding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupOnClickListener()
        analyticsManager.logScreenView(ScreenViewEvent.OnBoarding)
    }

    private fun setupToolbar() {
        with(activity as AuthenticationActivity) {
            view?.let { this.setLightStatusBar(it) }
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
            view_loading.isVisible = true
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.isVisible = false
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
