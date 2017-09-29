package chat.rocket.android.widget.internal

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import chat.rocket.android.widget.R
import chat.rocket.android.widget.helper.DrawableHelper
import kotlinx.android.synthetic.main.room_list_item.view.*

/**
 * Room list-item view used in sidebar.
 */
class RoomListItemView : FrameLayout {
    lateinit private var roomId: String
    private val privateChannelDrawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_lock_white_24dp, null)
    private val publicChannelDrawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_hashtag_white_24dp, null)
    private val liveChatChannelDrawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_livechat_white_24dp, null)
    private val userStatusDrawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_user_status_black_24dp, null)?.mutate()

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(context)
    }

    private fun initialize(context: Context) {
        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        val array = context
                .theme
                .obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground))

        background = array.getDrawable(0)
        array.recycle()

        View.inflate(context, R.layout.room_list_item, this)
    }

    fun setRoomId(roomId: String) {
        this.roomId = roomId
    }

    fun setUnreadCount(count: Int) {
        if (count > 0) {
            alertCount.text = count.toString()
            alertCount.visibility = View.VISIBLE
        } else {
            alertCount.visibility = View.GONE
        }
    }

    fun setAlert(alert: Boolean) {
        alpha = if (alert) 1.0f else 0.62f
    }

    fun setRoomName(roomName: String) {
        name.text = roomName
    }

    fun showPrivateChannelIcon() {
        type.setImageDrawable(privateChannelDrawable)
        userStatus.visibility = View.GONE
        type.visibility = View.VISIBLE
    }

    fun showPublicChannelIcon() {
        type.setImageDrawable(publicChannelDrawable)
        userStatus.visibility = View.GONE
        type.visibility = View.VISIBLE
    }

    fun showLivechatChannelIcon() {
        type.setImageDrawable(liveChatChannelDrawable)
        userStatus.visibility = View.GONE
        type.visibility = View.VISIBLE
    }

    fun showOnlineUserStatusIcon() {
        prepareDrawableAndShow(R.color.color_user_status_online)
    }

    fun showBusyUserStatusIcon() {
        prepareDrawableAndShow(R.color.color_user_status_busy)
    }

    fun showAwayUserStatusIcon() {
        prepareDrawableAndShow(R.color.color_user_status_away)
    }

    fun showOfflineUserStatusIcon() {
        prepareDrawableAndShow(R.color.color_user_status_offline)
    }

    private fun prepareDrawableAndShow(@ColorRes resId: Int) {
        DrawableHelper.wrapDrawable(userStatusDrawable)
        DrawableHelper.tintDrawable(userStatusDrawable, context, resId)
        userStatus.setImageDrawable(userStatusDrawable)
        type.visibility = View.GONE
        userStatus.visibility = View.VISIBLE
    }
}