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
import chat.rocket.android.thememanager.util.ThemeUtil
import chat.rocket.android.userdetails.presentation.UserDetailsPresenter
import chat.rocket.android.userdetails.presentation.UserDetailsView
import chat.rocket.android.util.extensions.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
        tintText()
        tintDrawables()
        setupListeners()
        presenter.loadUserDetails(userId)

        analyticsManager.logScreenView(ScreenViewEvent.UserDetails)
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    override fun showUserDetailsAndActions(
        avatarUrl: String?,
        name: String?,
        username: String?,
        status: String?,
        utcOffset: String?,
        isVideoCallAllowed: Boolean
    ) {
        val requestBuilder = Glide.with(this)
            .load(avatarUrl)
            .apply(RequestOptions.skipMemoryCacheOf(true))
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))

        requestBuilder.apply(
            RequestOptions.bitmapTransform(MultiTransformation(BlurTransformation(), CenterCrop()))
        ).into(image_blur)

        requestBuilder.apply(RequestOptions.bitmapTransform(RoundedCorners(14)))
            .into(image_avatar)

        text_name.text = name ?: getString(R.string.msg_unknown)
        text_username.text = username ?: getString(R.string.msg_unknown)

        text_description_status.text = status?.capitalize() ?: getString(R.string.msg_unknown)

        text_description_timezone.text = utcOffset ?: getString(R.string.msg_unknown)

        text_video_call.isVisible = isVideoCallAllowed

        // We should also setup the user details listeners.
        username?.run {
            text_message.setOnClickListener { presenter.createDirectMessage(this) }
            if (isVideoCallAllowed) {
                text_video_call.setOnClickListener { presenter.toVideoConference(this) }
            }
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
                    setInvisibleStatusBar(
                        it,
                        ThemeUtil.getThemeColor(R.attr.colorSettingsSecondaryBackground)
                    )
                }
                toolbar.isVisible = false
            }
        }, 400)
    }

    private fun setupListeners() {
        image_arrow_back.setOnClickListener { activity?.onBackPressed() }
    }

    private fun tintText(){
        text_message.setTextColor(ThemeUtil.getThemeColor(R.attr.colorAccent))
        text_video_call.setTextColor(ThemeUtil.getThemeColor(R.attr.colorAccent))
    }

    private fun tintDrawables(){
        ui{
            val drawableMessage = DrawableHelper.getDrawableFromId(R.drawable.ic_message_24dp, it)
            val drawableVideo = DrawableHelper.getDrawableFromId(R.drawable.ic_video_24dp, it)
            val drawables = arrayOf(drawableMessage, drawableVideo)
            DrawableHelper.tintDrawables(drawables, it, ThemeUtil.getThemeColorResource(R.attr.colorAccent))
            DrawableHelper.compoundTopDrawable(text_message,drawableMessage)
            DrawableHelper.compoundTopDrawable(text_video_call,drawableVideo)
        }
    }
}
