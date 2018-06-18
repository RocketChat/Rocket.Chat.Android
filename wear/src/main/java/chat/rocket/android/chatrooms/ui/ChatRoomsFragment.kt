package chat.rocket.android.chatrooms.ui

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.chatrooms.presentation.ChatRoomsPresenter
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.main.ui.MainNavigator
import chat.rocket.android.util.showToast
import chat.rocket.android.util.ui
import chat.rocket.core.model.ChatRoom
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import javax.inject.Inject

class ChatRoomsFragment : Fragment(), ChatRoomsView {
    @Inject
    lateinit var presenter: ChatRoomsPresenter
    @Inject
    lateinit var navigator: MainNavigator

    private lateinit var adapter: ChatRoomsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater!!.inflate(R.layout.fragment_chat_rooms, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        presenter.loadChatRooms()
    }

    override fun showLoading() {
        ui {
            view_loading.visibility = View.VISIBLE
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.visibility = View.GONE
        }
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun updateChatRooms(chatRooms: List<ChatRoom>) {
        adapter = ChatRoomsAdapter(context, chatRooms) { chatRoomId, chatRoomName, chatRoomType ->
            navigator.toChatRoom(chatRoomId, chatRoomName, chatRoomType)
        }
        ui { channels_list.adapter = adapter }
    }

    private fun setUpRecyclerView() {
        channels_list.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        channels_list.itemAnimator = DefaultItemAnimator()
        channels_list.isCircularScrollingGestureEnabled = true
        channels_list.scrollDegreesPerScreen = 90f
    }
}