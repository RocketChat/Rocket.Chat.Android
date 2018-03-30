package chat.rocket.android.chatrooms.ui

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import chat.rocket.android.R
import chat.rocket.android.chatrooms.presentation.ChatRoomsPresenter
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.util.extensions.*
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.core.internal.realtime.State
import chat.rocket.core.model.ChatRoom
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class ChatRoomsFragment : Fragment(), ChatRoomsView {
    @Inject lateinit var presenter: ChatRoomsPresenter
    @Inject lateinit var serverInteractor: GetCurrentServerInteractor
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var localRepository: LocalRepository
    private var searchView: SearchView? = null
    private val handler = Handler()

    private var listJob: Job? = null

    companion object {
        fun newInstance() = ChatRoomsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        setHasOptionsMenu(true)
    }

    override fun onDestroy() {
        handler.removeCallbacks(dismissStatus)
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

    override fun onDestroyView() {
        listJob?.cancel()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chatrooms, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView
        searchView?.maxWidth = Integer.MAX_VALUE
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return queryChatRoomsByName(query)
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return queryChatRoomsByName(newText)
            }
        })
    }

    override suspend fun updateChatRooms(newDataSet: List<ChatRoom>) {
        activity?.apply {
            listJob?.cancel()
            listJob = launch(UI) {
                val adapter = recycler_view.adapter as ChatRoomsAdapter
                // FIXME https://fabric.io/rocketchat3/android/apps/chat.rocket.android.dev/issues/5a90d4718cb3c2fa63b3f557?time=last-seven-days
                // TODO - fix this bug to reenable DiffUtil
                val diff = async(CommonPool) {
                    DiffUtil.calculateDiff(RoomsDiffCallback(adapter.dataSet, newDataSet))
                }.await()

                if (isActive) {
                    adapter.updateRooms(newDataSet)
                    diff.dispatchUpdatesTo(adapter)
                }
            }
        }
    }

    override fun showNoChatRoomsToDisplay() = text_no_data_to_display.setVisible(true)

    override fun showLoading() = view_loading.setVisible(true)

    override fun hideLoading() {
        if (view_loading != null) {
            view_loading.setVisible(false)
        }
    }

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun showConnectionState(state: State) {
        activity?.apply {
            connection_status_text.fadeIn()
            handler.removeCallbacks(dismissStatus)
            when (state) {
                is State.Connected -> {
                    connection_status_text.text = getString(R.string.status_connected)
                    handler.postDelayed(dismissStatus, 2000)
                }
                is State.Disconnected -> connection_status_text.text = getString(R.string.status_disconnected)
                is State.Connecting -> connection_status_text.text = getString(R.string.status_connecting)
                is State.Authenticating -> connection_status_text.text = getString(R.string.status_authenticating)
                is State.Disconnecting -> connection_status_text.text = getString(R.string.status_disconnecting)
                is State.Waiting -> connection_status_text.text = getString(R.string.status_waiting, state.seconds)
            }
        }
    }

    private val dismissStatus = {
        if (connection_status_text != null) {
            connection_status_text.fadeOut()
        }
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_chats)
    }

    private fun setupRecyclerView() {
        activity?.apply {
            recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recycler_view.addItemDecoration(DividerItemDecoration(this,
                    resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_start),
                    resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_end)))
            recycler_view.itemAnimator = DefaultItemAnimator()
            // TODO - use a ViewModel Mapper instead of using settings on the adapter
            recycler_view.adapter = ChatRoomsAdapter(
                    this,
                    settingsRepository.get(serverInteractor.get()!!), localRepository) { chatRoom ->
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