package chat.rocket.android.fragment.chatroom.list

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.layouthelper.chatroom.list.RoomFileListAdapter
import chat.rocket.android.layouthelper.chatroom.list.RoomMemberListAdapter
import chat.rocket.android.layouthelper.chatroom.list.RoomPinnedMessagesAdapter
import chat.rocket.core.models.Message
import chat.rocket.core.models.User
import kotlinx.android.synthetic.main.fragment_room_list.*

class RoomListFragment : Fragment(), RoomListContract.View {
    var actionId: Int = 0
    lateinit var roomId: String
    lateinit var roomType: String
    lateinit var hostname: String
    lateinit var token: String
    lateinit var userId: String
    lateinit var presenter: RoomListPresenter

    companion object {
        fun newInstance(actionId: Int,
                        roomId: String,
                        roomType: String,
                        hostname: String,
                        token: String,
                        userId: String): RoomListFragment {
            val args = Bundle()
            args.putInt("actionId", actionId)
            args.putString("roomId", roomId)
            args.putString("roomType", roomType)
            args.putString("hostname", hostname)
            args.putString("token", token)
            args.putString("userId", userId)

            val roomFileListDialogFragment = RoomListFragment()
            roomFileListDialogFragment.arguments = args

            return roomFileListDialogFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionId = arguments.getInt("actionId")
        roomId = arguments.getString("roomId")
        roomType = arguments.getString("roomType")
        hostname = arguments.getString("hostname")
        token = arguments.getString("token")
        userId = arguments.getString("userId")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(R.layout.fragment_room_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = RoomListPresenter(context, this)
    }

    override fun onResume() {
        super.onResume()
        when (actionId) {
            R.id.action_pinned_messages -> {
                presenter.requestPinnedMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
            R.id.action_favorite_messages -> {
                presenter.requestFavoriteMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
            R.id.action_file_list -> {
                presenter.requestFileList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
            R.id.action_member_list -> {
                presenter.requestMemberList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
        }
    }

    override fun showPinnedMessages(dataSet: ArrayList<Message>) {
        if (dataSet.isEmpty()) {
            showMessage(getString(R.string.fragment_room_list_no_pinned_message_to_show))
        } else {
            recyclerView.adapter = RoomPinnedMessagesAdapter(dataSet, hostname)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        }
    }

    override fun showFavoriteMessages(dataSet: ArrayList<Message>) {
        if (dataSet.isEmpty()) {
            showMessage(getString(R.string.fragment_room_list_no_pinned_message_to_show))
        } else {
            recyclerView.adapter = RoomPinnedMessagesAdapter(dataSet, hostname)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        }
    }

    override fun showFileList(dataSet: ArrayList<String>) {
        if (dataSet.isEmpty()) {
            showMessage(getString(R.string.fragment_room_list_no_favorite_message_to_show))
        } else {
            recyclerView.adapter = RoomFileListAdapter(dataSet)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        }
    }

    override fun showMemberList(dataSet: ArrayList<User>) {
        if (dataSet.isEmpty()) {
            showMessage(getString(R.string.fragment_room_list_no_member_list_to_show))
        } else {
            recyclerView.adapter = RoomMemberListAdapter(dataSet)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        }
    }

    override fun showMessage(message: String) {
        messageText.text = message
        messageText.visibility = View.VISIBLE
    }
}