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
import chat.rocket.android.layouthelper.chatroom.list.RoomMessagesAdapter
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
    var isDataRequested: Boolean = false

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
        if (!isDataRequested) {
            requestData()
            isDataRequested = true
        }
    }

    private fun requestData() {
        when (actionId) {
            R.id.action_pinned_messages -> {
                activity.title = getString(R.string.fragment_room_list_pinned_message_title)
                presenter.requestPinnedMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
            R.id.action_favorite_messages -> {
                activity.title = getString(R.string.fragment_room_list_favorite_message_title)
                presenter.requestFavoriteMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
//            R.id.action_file_list -> {
//                activity.title = getString(R.string.fragment_room_list_file_list_title)
//                presenter.requestFileList(roomId,
//                        roomType,
//                        hostname,
//                        token,
//                        userId)
//            }
            R.id.action_member_list -> {
                activity.title = getString(R.string.fragment_room_list_member_list_title)
                presenter.requestMemberList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
        }
    }

    override fun showPinnedMessages(dataSet: ArrayList<Message>) {
        waitingView.visibility = View.GONE
        if (dataSet.isEmpty()) {
            showMessage(getString(R.string.fragment_room_list_no_pinned_message_to_show))
        } else {
            recyclerView.adapter = RoomMessagesAdapter(dataSet, hostname, context)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun showFavoriteMessages(dataSet: ArrayList<Message>) {
        if (dataSet.isEmpty()) {
            showMessage(getString(R.string.fragment_room_list_no_favorite_message_to_show))
        } else {
            waitingView.visibility = View.GONE
            recyclerView.adapter = RoomMessagesAdapter(dataSet, hostname, context)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun showFileList(dataSet: ArrayList<String>) {
        if (dataSet.isEmpty()) {
            showMessage(getString(R.string.fragment_room_list_no_file_list_to_show))
        } else {
            waitingView.visibility = View.GONE
            recyclerView.adapter = RoomFileListAdapter(dataSet)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun showMemberList(dataSet: ArrayList<User>, total: String) {
        if (dataSet.isEmpty()) {
            showMessage(getString(R.string.fragment_room_list_no_member_list_to_show))
        } else {
            waitingView.visibility = View.GONE
            activity.title = getString(R.string.fragment_room_list_member_list_title) + " (" + total + ")"
            recyclerView.adapter = RoomMemberListAdapter(dataSet, hostname, context)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun showMessage(message: String) {
        waitingView.visibility = View.GONE
        messageText.text = message
        messageText.visibility = View.VISIBLE
    }
}