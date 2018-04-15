package chat.rocket.android.chatroom.information.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.information.presentation.InformationPresenter
import chat.rocket.android.chatroom.information.presentation.InformationView
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.util.extensions.*
import chat.rocket.common.model.RoomType
import chat.rocket.core.model.ChatRoom
import com.facebook.drawee.view.SimpleDraweeView
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_room_info.*
import javax.inject.Inject

fun newInstance(chatRoomId: String, chatRoomType: String, isSubscribed: Boolean): Fragment {
    return InformationFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
            putBoolean(BUNDLE_CHAT_ROOM_IS_SUBSCRIBED, isSubscribed)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_CHAT_ROOM_TYPE = "chat_room_type"
private const val BUNDLE_CHAT_ROOM_IS_SUBSCRIBED = "chat_room_is_subscribed"

class InformationFragment: Fragment(), InformationView {
    @Inject lateinit var presenter: InformationPresenter

    private lateinit var chatRoom: ChatRoom
    private lateinit var chatRoomId: String
    private lateinit var chatRoomType: String
    private var isSubscribed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomType = bundle.getString(BUNDLE_CHAT_ROOM_TYPE)
            isSubscribed = bundle.getBoolean(BUNDLE_CHAT_ROOM_IS_SUBSCRIBED)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_chat_room_info)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        presenter.loadRoomInfo(chatRoomId, chatRoomType, isSubscribed)
        setupButtons()
        activity?.apply {
            (this as? ChatRoomActivity)?.showRoomTypeIcon(false)
        }
    }

    override fun showLoading() {
        ui {
            progress.setVisible(true)
            room_info_layout.visibility = View.GONE
        }
    }

    override fun hideLoading() {
        ui {
            progress.setVisible(false)
            room_info_layout.visibility = View.VISIBLE
        }
    }

    override fun showRoomInfo(room: ChatRoom) {
        chatRoom = room

        room_name.text = getDisplayedText(room.name)
        bindChannelType(room.type, room_name)

        room_description_text.text = getDisplayedText(room.description)
        room_topic_text.text = getDisplayedText(room.topic)
        room_announcement_text.text = getDisplayedText(room.announcement)

        if (room.readonly == true) room_read_only_status.visibility = View.VISIBLE

        bindAvatar(room_avatar, room)
    }

    override fun onLeave() {
        activity?.finish()
        activity?.overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
    }

    override fun onHide() {
        onLeave()
    }

    override fun allowRoomEditing() {
        edit_button.visibility = View.VISIBLE
    }

    override fun allowHideAndLeave(isOpen: Boolean) {
        hide_button.visibility = View.VISIBLE
        hide_button.text = if (isOpen) resources.getString(R.string.action_hide) else resources.getString(R.string.action_show)

        leave_button.visibility = View.VISIBLE
    }

    override fun showGenericErrorMessage() {
        ui {
            showToast(getString(R.string.msg_generic_error))
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

    private fun setupToolbar() {
        (activity as ChatRoomActivity).setupToolbarTitle(getString(R.string.title_chat_room_info))
    }

    private fun setupButtons() {
        edit_button.setOnClickListener {
            presenter.toEditChatInfo(chatRoomId, chatRoomType)
        }
        hide_button.setOnClickListener {
            if (chatRoom.open)
                presenter.hideOrShowChat(chatRoomId, chatRoomType, true)
            else
                presenter.hideOrShowChat(chatRoomId, chatRoomType, false)
        }
        leave_button.setOnClickListener {
            presenter.leaveChat(chatRoomId, chatRoomType)
        }
    }

    private fun bindAvatar(drawable: SimpleDraweeView, room: ChatRoom) {
        if (room.type is RoomType.DirectMessage) {
            drawable.setImageURI(room.client.url.avatarUrl(chatRoom.name))
        } else {
            drawable.setImageURI(room.client.url.avatarUrl(chatRoom.name, true))
        }
    }

    private fun bindChannelType(type: RoomType, text: TextView) {
        val imageType = when(type) {
            is RoomType.Channel -> {
                DrawableHelper.getDrawableFromId(R.drawable.ic_room_channel, context!!)
            }
            is RoomType.PrivateGroup -> {
                DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, context!!)
            }
            else -> null
        }

        imageType?.let {
            val wrappedImage = DrawableHelper.wrapDrawable(it)
            DrawableHelper.compoundDrawable(text, wrappedImage.mutate())
        }
    }

    private fun getDisplayedText(text: String?): String {
        return if (text.isNullOrBlank()) getString(R.string.msg_not_available) else text!!
    }
}
