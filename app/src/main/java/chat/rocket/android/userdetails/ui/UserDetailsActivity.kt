package chat.rocket.android.userdetails.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import blurred
import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.emoji.internal.GlideApp
import chat.rocket.android.userdetails.presentation.UserDetailsPresenter
import chat.rocket.android.userdetails.presentation.UserDetailsView
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extension.orFalse
import chat.rocket.android.util.extensions.showToast
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.userId
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_user_details.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToLong

fun Context.userDetailsIntent(userId: String, subscriptionId: String): Intent {
    return Intent(this, UserDetailsActivity::class.java).apply {
        putExtra(EXTRA_USER_ID, userId)
        putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)
    }
}

const val EXTRA_USER_ID = "EXTRA_USER_ID"
const val EXTRA_SUBSCRIPTION_ID = "EXTRA_USERNAME"

class UserDetailsActivity : AppCompatActivity(), UserDetailsView, HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var presenter: UserDetailsPresenter

    private lateinit var subscriptionId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        setupToolbar()

        val userId = intent.getStringExtra(EXTRA_USER_ID)
        subscriptionId = intent.getStringExtra(EXTRA_SUBSCRIPTION_ID)
        showLoadingView(true)
        presenter.loadUserDetails(userId = userId)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentDispatchingAndroidInjector

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun showUserDetails(
        avatarUrl: String?,
        username: String?,
        name: String?,
        utcOffset: Float?,
        status: String,
        chatRoom: ChatRoom?
    ) {
        text_view_name.text = name
        text_view_username.text = username
        text_view_status.text = status.capitalize()

        launch(UI) {
            val image = withContext(CommonPool) {
                val requestOptions = RequestOptions()
                    .priority(Priority.IMMEDIATE)
                    .transforms(CenterInside(), FitCenter())

                return@withContext GlideApp.with(this@UserDetailsActivity)
                    .asBitmap()
                    .load(avatarUrl)
                    .apply(requestOptions)
                    .submit()
                    .get().also { showLoadingView(false) }
            }

            val blurredBitmap = image.blurred(context = this@UserDetailsActivity,
                newWidth = toolbar.measuredWidth, newHeight =  toolbar.measuredHeight)
            toolbar.background = BitmapDrawable(resources, blurredBitmap)
            GlideApp.with(this@UserDetailsActivity)
                .asBitmap()
                .transforms(RoundedCorners(25), CenterCrop())
                .load(avatarUrl)
                .into(image_view_avatar)
        }

        utcOffset?.let {
            val offsetLong = it.roundToLong()
            val offset = if (it > 0) "+$offsetLong" else offsetLong.toString()
            val formatter = DateTimeFormatter.ofPattern("'(GMT$offset)' hh:mm a")
            val zoneId = ZoneId.systemDefault()
            val timeNow = OffsetDateTime.now(ZoneOffset.UTC).plusHours(offsetLong).toLocalDateTime()
            text_view_tz.text = formatter.format(ZonedDateTime.of(timeNow, zoneId))
        }

        text_view_message.setOnClickListener {
            toDirectMessage(chatRoom = chatRoom)
        }

        image_view_message.setOnClickListener {
            toDirectMessage(chatRoom = chatRoom)
        }
    }

    private fun showLoadingView(show: Boolean) {
        runOnUiThread {
            group_user_details.isVisible = !show
            view_loading.isVisible = show
        }
    }

    private fun toDirectMessage(chatRoom: ChatRoom?) {
        chatRoom?.let { c ->
            finish()
            if (c.subscriptionId.isEmpty() || c.subscriptionId != subscriptionId) {
                startActivity(
                    chatRoomIntent(
                        chatRoomId = c.id,
                        chatRoomName = c.name,
                        chatRoomType = c.type.toString(),
                        isReadOnly = c.readonly.orFalse(),
                        chatRoomLastSeen = c.lastSeen ?: 0,
                        isSubscribed = c.open,
                        isCreator = false,
                        isFavorite = c.favorite
                    )
                )
            }
        }
    }
}
