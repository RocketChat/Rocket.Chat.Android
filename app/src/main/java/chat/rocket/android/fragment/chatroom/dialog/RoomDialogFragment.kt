package chat.rocket.android.fragment.chatroom.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.layouthelper.chatroom.dialog.RoomFileListAdapter
import chat.rocket.android.layouthelper.chatroom.dialog.RoomMemberListAdapter
import chat.rocket.android.layouthelper.chatroom.dialog.RoomPinnedMessagesAdapter
import chat.rocket.core.models.Message

/**
 * Displays a dialog containing pinned messages, favorite messages, file list or member list of a room.
 */
class RoomDialogFragment : DialogFragment(), RoomDialogContract.View {
    lateinit var roomId: String
    lateinit var roomType: String
    lateinit var hostname: String
    lateinit var token: String
    lateinit var userId: String
    private var actionId: Int = 0
    private lateinit var closeButton: ImageButton
    lateinit var messageText: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var presenter: RoomDialogContract.Presenter

    companion object {
        fun newInstance(roomId: String,
                        roomType: String,
                        hostname: String,
                        token: String,
                        userId: String,
                        actionId: Int): RoomDialogFragment {
            val args = Bundle()
            args.putString("roomId", roomId)
            args.putString("roomType", roomType)
            args.putString("hostname", hostname)
            args.putString("token", token)
            args.putString("userId", userId)
            args.putInt("actionId", actionId)

            val roomFileListDialogFragment = RoomDialogFragment()
            roomFileListDialogFragment.arguments = args

            return roomFileListDialogFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomId = arguments.getString("roomId")
        roomType = arguments.getString("roomType")
        hostname = arguments.getString("hostname")
        token = arguments.getString("token")
        userId = arguments.getString("userId")
        actionId = arguments.getInt("actionId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_room, container, false)

        messageText = view.findViewById(R.id.text_message)
        closeButton = view.findViewById(R.id.button_close)
        closeButton.setOnClickListener { dismissDialogFragment() }
        recyclerView = view.findViewById(R.id.recycler_view)

        presenter = RoomDialogPresenter(context, this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.getDataSet(roomId,
                roomType,
                hostname,
                token,
                userId,
                actionId)
    }

    override fun showPinnedMessages(dataSet: ArrayList<Message>) {
        if (dataSet.isEmpty()) {
            // TODO("move to strings.xml")
            showMessage("None pinned message to show")
        } else {
            recyclerView.adapter = RoomPinnedMessagesAdapter(dataSet, hostname)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        }
    }

    override fun showFavoriteMessages() {
       // TODO("not implemented")
    }

    override fun showFileList(dataSet: ArrayList<String>) {
        if (dataSet.isEmpty()) {
            // TODO("move to strings.xml")
            showMessage("None file to show")
        } else {
            recyclerView.adapter = RoomFileListAdapter(dataSet)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        }
    }

    override fun showMemberList(dataSet: ArrayList<String>) {
        if (dataSet.isEmpty()) {
            // TODO("move to strings.xml")
            showMessage("None member to show")
        } else {
            recyclerView.adapter = RoomMemberListAdapter(dataSet)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        }
    }

    override fun showMessage(message: String) {
        messageText.text = message
        messageText.visibility = View.VISIBLE
    }

    private fun dismissDialogFragment() {
        super.dismiss()
    }
}