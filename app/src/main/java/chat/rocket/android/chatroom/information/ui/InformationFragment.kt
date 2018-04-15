package chat.rocket.android.chatroom.information.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.information.presentation.InformationPresenter
import chat.rocket.android.chatroom.information.presentation.InformationView
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.common.model.RoomType
import chat.rocket.core.model.ChatRoom
import com.facebook.drawee.view.SimpleDraweeView
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_room_info.*
import javax.inject.Inject

fun newInstance(chatRoomId: String): Fragment {
    return InformationFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"

class InformationFragment: Fragment(), InformationView {
    @Inject lateinit var presenter: InformationPresenter

    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
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
        presenter.loadRoomInfo(chatRoomId)
    }

    override fun showLoading() {
        progress?.show()
    }

    override fun hideLoading() {
        progress?.hide()
    }

    override fun showRoomInfo(room: ChatRoom) {
        room_name.text = getDisplayedText(room.name)
        bindChannelType(room.type, room_name)

        room_description_text.text = getDisplayedText(room.description)
        room_topic_text.text = getDisplayedText(room.topic)
        room_announcement_text.text = getDisplayedText(room.announcement)

        if (room.readonly == true) room_read_only_status.visibility = View.VISIBLE

        bindAvatar(room_avatar, room)
    }

    override fun showGenericErrorMessage() {
        (getString(R.string.msg_generic_error))
    }

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    private fun setupToolbar() {
        (activity as ChatRoomActivity).setupToolbarTitle(getString(R.string.title_chat_room_info))
    }

    private fun bindAvatar(drawable: SimpleDraweeView, room: ChatRoom) {
        drawable.setImageURI(UrlHelper.getAvatarUrl(room.client.url, "@${room.name}", "png"))
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
