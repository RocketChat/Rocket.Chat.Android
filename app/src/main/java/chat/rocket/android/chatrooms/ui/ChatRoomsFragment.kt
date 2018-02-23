package chat.rocket.android.chatrooms.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import chat.rocket.android.R
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.chatrooms.presentation.ChatRoomsPresenter
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.core.model.ChatRoom
import com.bumptech.glide.util.FixedPreloadSizeProvider
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject



class ChatRoomsFragment : Fragment(), ChatRoomsView {
    @Inject lateinit var presenter: ChatRoomsPresenter
    private var searchView: SearchView? = null

    companion object {
        fun newInstance() = ChatRoomsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        setHasOptionsMenu(true)
    }

    override fun onDestroy() {
        presenter.disconnect()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_chat_rooms)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        presenter.loadChatRooms()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chatrooms, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return queryChatRoomsByName(query)
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return queryChatRoomsByName(newText)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_logout -> presenter.logout()
        }
        return true
    }

    override suspend fun updateChatRooms(newDataSet: List<ChatRoom>) {
        activity.apply {
            launch(UI) {
                val adapter = recycler_view.adapter as ChatRoomsAdapter
                val diff = async(CommonPool) {
                    DiffUtil.calculateDiff(RoomsDiffCallback(adapter.dataSet, newDataSet))
                }.await()

                adapter.updateRooms(newDataSet)
                diff.dispatchUpdatesTo(adapter)
            }
        }
    }

    override fun showNoChatRoomsToDisplay() = text_no_data_to_display.setVisible(true)

    override fun showLoading() = view_loading.setVisible(true)

    override fun hideLoading() = view_loading.setVisible(false)

    override fun showMessage(resId: Int) = showToast(resId)

    override fun showMessage(message: String) = showToast(message)

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun onLogout() {
        activity?.apply {
            finish()
            val intent = Intent(this, AuthenticationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_chats)
    }

    private fun setupRecyclerView() {
        activity?.apply {
            val size = resources.getDimensionPixelSize(R.dimen.avatar_size)
            val sizeProvider = FixedPreloadSizeProvider<ChatRoom>(size, size)
            recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recycler_view.addItemDecoration(DividerItemDecoration(this, 144, 32))
            recycler_view.itemAnimator = DefaultItemAnimator()
            recycler_view.adapter = ChatRoomsAdapter(this) { chatRoom ->
                presenter.loadChatRoom(chatRoom)
            }
        }
    }

    private fun queryChatRoomsByName(name: String?): Boolean {
        presenter.chatRoomsByName(name ?: "")
        return true
    }

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
