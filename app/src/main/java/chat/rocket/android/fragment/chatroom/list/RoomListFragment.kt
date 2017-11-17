package chat.rocket.android.fragment.chatroom.list

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.layouthelper.chatroom.list.RoomFileListAdapter
import chat.rocket.android.layouthelper.chatroom.list.RoomMemberListAdapter
import chat.rocket.android.layouthelper.chatroom.list.RoomMessagesAdapter
import chat.rocket.core.models.Attachment
import chat.rocket.core.models.Message
import chat.rocket.core.models.User
import kotlinx.android.synthetic.main.fragment_room_list.*
import java.util.*

/**
 * Created by Filipe de Lima Brito (filipedelimabrito@gmail.com) on 9/22/17.
 */
class RoomListFragment : Fragment(), RoomListContract.View {

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
        activity?.title = ""
        val args = arguments
        args?.let {
            actionId = args.getInt("actionId")
            roomId = args.getString("roomId")
            roomType = args.getString("roomType")
            hostname = args.getString("hostname")
            token = args.getString("token")
            userId = args.getString("userId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_room_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = RoomListPresenter(context!!, this)
    }

    override fun onResume() {
        super.onResume()
        if (!isDataRequested) {
            requestData(0)
            isDataRequested = true
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.cancelRequest()
    }

    private fun requestData(offset: Int) {
        when (actionId) {
            R.id.action_pinned_messages -> {
                presenter.requestPinnedMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId,
                        offset)
            }
            R.id.action_file_list -> {
                presenter.requestFileList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId,
                        offset)
            }
            R.id.action_favorite_messages -> {
                presenter.requestFavoriteMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId,
                        offset)
            }
            R.id.action_member_list -> {
                presenter.requestMemberList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId,
                        offset)
            }
        }
    }

    override fun showPinnedMessages(dataSet: ArrayList<Message>, total: String) {
        activity?.title = getString(R.string.fragment_room_list_pinned_message_title, total)
        if (recyclerView.adapter == null) {
            recyclerView.adapter = RoomMessagesAdapter(dataSet, hostname, context!!)
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.layoutManager = linearLayoutManager
            if (dataSet.size >= 50) {
                recyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, recyclerView: RecyclerView?) {
                        loadNextDataFromApi(page)
                    }
                })
            }
        } else {
            (recyclerView.adapter as RoomMessagesAdapter).addDataSet(dataSet)
        }
    }

    override fun showFavoriteMessages(dataSet: ArrayList<Message>, total: String) {
        activity?.title = getString(R.string.fragment_room_list_favorite_message_title, total)
        if (recyclerView.adapter == null) {
            recyclerView.adapter = RoomMessagesAdapter(dataSet, hostname, context!!)
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.layoutManager = linearLayoutManager
            if (dataSet.size >= 50) {
                recyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, recyclerView: RecyclerView?) {
                        loadNextDataFromApi(page)
                    }
                })
            }
        } else {
            (recyclerView.adapter as RoomMessagesAdapter).addDataSet(dataSet)
        }
    }

    override fun showFileList(dataSet: ArrayList<Attachment>, total: String) {
        activity?.title = getString(R.string.fragment_room_list_file_list_title, total)
        if (recyclerView.adapter == null) {
            recyclerView.adapter = RoomFileListAdapter(dataSet)
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.layoutManager = linearLayoutManager
            if (dataSet.size >= 50) {
                recyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, recyclerView: RecyclerView?) {
                        loadNextDataFromApi(page)
                    }
                })
            }
        } else {
            (recyclerView.adapter as RoomFileListAdapter).addDataSet(dataSet)
        }
    }

    override fun showMemberList(dataSet: ArrayList<User>, total: String) {
        activity?.title = getString(R.string.fragment_room_list_member_list_title, total)
        if (recyclerView.adapter == null) {
            recyclerView.adapter = RoomMemberListAdapter(dataSet, hostname, context!!)
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.layoutManager = linearLayoutManager
            if (dataSet.size >= 50) {
                recyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, recyclerView: RecyclerView?) {
                        loadNextDataFromApi(page)
                    }
                })
            }
        } else {
            (recyclerView.adapter as RoomMemberListAdapter).addDataSet(dataSet)
        }
    }

    override fun showMessage(message: String) {
        messageText.text = message
        messageText.visibility = View.VISIBLE
    }

    override fun showWaitingView(shouldShow: Boolean) {
        if (shouldShow) {
            waitingView.visibility = View.VISIBLE
        } else {
            waitingView.visibility = View.GONE
        }
    }

    private fun loadNextDataFromApi(page: Int) {
        requestData(page * 50)
    }

    private var actionId: Int = 0
    private lateinit var roomId: String
    private lateinit var roomType: String
    private lateinit var hostname: String
    private lateinit var token: String
    private lateinit var userId: String
    private lateinit var presenter: RoomListContract.Presenter
    private var isDataRequested: Boolean = false
}