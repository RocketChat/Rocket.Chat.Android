package chat.rocket.android.userdetails.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.AndroidSupportInjection
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import kotlinx.android.synthetic.main.fragment_user_details.*
import javax.inject.Inject

fun newInstance(userId: String): Fragment = UserDetailsFragment().apply {
    arguments = Bundle(1).apply {
        putString(BUNDLE_USER_ID, userId)
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
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            userId = getString(BUNDLE_USER_ID, "")
        }
            ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_user_details)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
        presenter.loadUserDetails(userId)

        analyticsManager.logScreenView(ScreenViewEvent.UserDetails)
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    override fun showUserDetailsAndActions(
        avatarUrl: String,
        name: String,
        username: String,
        status: String,
        utcOffset: String,
        isVideoCallAllowed: Boolean
    ) {
        val requestBuilder = Glide.with(this).load(avatarUrl)

        requestBuilder.apply(
            RequestOptions.bitmapTransform(MultiTransformation(BlurTransformation(), CenterCrop()))
        ).into(image_blur)

        requestBuilder.apply(RequestOptions.bitmapTransform(RoundedCorners(14)))
            .into(image_avatar)

        text_name.text = name
        text_username.text = username
        text_description_status.text = status.substring(0, 1).toUpperCase() + status.substring(1)
        text_description_timezone.text = utcOffset

        // We should also setup the user details listeners.
        text_message.setOnClickListener { presenter.createDirectMessage(username) }

        if (isVideoCallAllowed) {
            text_video_call.isVisible = true
            text_video_call.setOnClickListener { presenter.toVideoConference(username) }
        } else {
            text_video_call.isVisible = false
        }
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
        handler.postDelayed({
            with(activity as ChatRoomActivity) {
                view?.let {
                    setLightStatusBar(
                        it,
                        ContextCompat.getColor(this, R.color.whitesmoke)
                    )
                }
                toolbar.isVisible = false
            }
        }, 400)
    }

    private fun setupListeners() {
        image_arrow_back.setOnClickListener { activity?.onBackPressed() }
    }
}