package chat.rocket.android.chatrooms.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.chatrooms.presentation.ChatRoomsPresenter
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.core.model.ChatRoom
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class ChatRoomsFragment : Fragment(), ChatRoomsView {
    @Inject lateinit var presenter: ChatRoomsPresenter

    companion object {
        fun newInstance() = ChatRoomsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_chat_rooms, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floating_search_view.setOnQueryChangeListener { oldQuery, newQuery ->
            floating_search_view.showProgress()
            presenter.chatRoomsByName(newQuery)
            if (oldQuery.isNotEmpty() && newQuery.isEmpty()) {
                floating_search_view.clearSuggestions()
                floating_search_view.hideProgress()
            }
        }

        activity?.apply {
            recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recycler_view.addItemDecoration(DividerItemDecoration(this, 144, 32))
            recycler_view.adapter = ChatRoomsAdapter(this)
            recycler_view.itemAnimator = DefaultItemAnimator()
        }
        presenter.loadChatRooms()
    }

    /*override fun showChatRooms(dataSet: MutableList<ChatRoom>) {
        floating_search_view.hideProgress()
    }*/

    override suspend fun updateChatRooms(newDataSet: List<ChatRoom>) {
        activity.apply {
            launch(UI) {
                val adapter = recycler_view.adapter as ChatRoomsAdapter
                val diff = async(CommonPool) {
                    DiffUtil.calculateDiff(RoomsDiffCallback(adapter.dataSet, newDataSet))
                }.await()

                floating_search_view.hideProgress()
                adapter.updateRooms(newDataSet)
                diff.dispatchUpdatesTo(adapter)
            }
        }
    }

    override fun showLoading() = view_loading.show()

    override fun hideLoading() = view_loading.hide()

    override fun showMessage(message: String) = Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    class RoomsDiffCallback(private val oldRooms: List<ChatRoom>,
                            private val newRooms: List<ChatRoom>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldRooms[oldItemPosition].id == newRooms[newItemPosition].id
        }

        override fun getOldListSize(): Int {
            return oldRooms.size
        }

        override fun getNewListSize(): Int {
            return newRooms.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldRooms[oldItemPosition].updatedAt == newRooms[newItemPosition].updatedAt
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return newRooms[newItemPosition]
        }
    }
}