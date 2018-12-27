package chat.rocket.android.userdetails.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.userdetails.presentation.UserDetailsPresenter
import chat.rocket.android.userdetails.presentation.UserDetailsView
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setLightStatusBar
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import com.bumptech.glide.Glide
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import kotlinx.android.synthetic.main.fragment_user_details.*
import javax.inject.Inject

fun newInstance(userId: String): Fragment {
    return UserDetailsFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_USER_ID, userId)
        }
    }
}

internal const val TAG_USER_DETAILS_FRAGMENT = "UserDetailsFragment"
private const val BUNDLE_USER_ID = "user_id"

class UserDetailsFragment : Fragment(), UserDetailsView {
    @Inject
    lateinit var presenter: UserDetailsPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val bundle = arguments
        if (bundle != null) {
            userId = bundle.getString(BUNDLE_USER_ID)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_user_details)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        presenter.loadUserDetails(userId)

        analyticsManager.logScreenView(ScreenViewEvent.UserDetails)
    }

    override fun showUserDetails(
        avatarUrl: String,
        name: String,
        username: String,
        status: String,
        utcOffset: String
    ) {
        Glide.with(this)
            .asBitmap()
            .load(avatarUrl)
            .into(image_avatar)

        text_name.text = name
        text_username.text = username
        text_description_status.text = status
        text_description_timezone.text = utcOffset

        // We should also setup the user details listeners.
        text_message.setOnClickListener { presenter.createDirectMessage(username) }
    }

    override fun showLoading() {
        group_user_details.isVisible = false
        view_loading.isVisible = true
    }

    override fun hideLoading() {
        group_user_details.isVisible = true
        view_loading.isVisible = false
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    private fun setupToolbar() {
        with(activity as ChatRoomActivity) {
            view?.let { setLightStatusBar(it) }
            toolbar.isVisible = false
        }
    }
}