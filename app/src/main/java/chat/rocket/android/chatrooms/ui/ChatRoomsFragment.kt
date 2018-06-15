package chat.rocket.android.chatrooms.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.RoomsAdapter
import chat.rocket.android.chatrooms.infrastructure.ChatRoomsRepository
import chat.rocket.android.chatrooms.presentation.ChatRoomsPresenter
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.chatrooms.viewmodel.ChatRoomsViewModel
import chat.rocket.android.chatrooms.viewmodel.ChatRoomsViewModelFactory
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.helper.ChatRoomsSortOrder
import chat.rocket.android.helper.Constants
import chat.rocket.android.helper.SharedPreferenceHelper
import chat.rocket.android.util.extensions.fadeIn
import chat.rocket.android.util.extensions.fadeOut
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.model.ChatRoom
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

private const val BUNDLE_CHAT_ROOM_ID = "BUNDLE_CHAT_ROOM_ID"

class ChatRoomsFragment : Fragment(), ChatRoomsView {
    @Inject
    lateinit var presenter: ChatRoomsPresenter
    @Inject
    lateinit var factory: ChatRoomsViewModelFactory
    @Inject
    lateinit var dbManager: DatabaseManager // TODO - remove when moving ChatRoom screen to DB

    lateinit var viewModel: ChatRoomsViewModel

    private var searchView: SearchView? = null
    private val handler = Handler()

    private var chatRoomId: String? = null

    companion object {
        fun newInstance(chatRoomId: String? = null): ChatRoomsFragment {
            return ChatRoomsFragment().apply {
                arguments = Bundle(1).apply {
                    putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        setHasOptionsMenu(true)
        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomId?.let {
                // TODO - bring back support to load a room from id.
                //presenter.goToChatRoomWithId(it)
                chatRoomId = null
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(dismissStatus)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_chat_rooms)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this, factory).get(ChatRoomsViewModel::class.java)
        subscribeUi()

        setupToolbar()
    }

    private fun subscribeUi() {
        ui {

            val adapter = RoomsAdapter { roomId ->
                launch(UI) {
                    dbManager.getRoom(roomId)?.let { room ->
                        ui {
                            presenter.loadChatRoom(room)
                        }
                    }

                }
            }

            recycler_view.layoutManager = LinearLayoutManager(it)
            recycler_view.addItemDecoration(DividerItemDecoration(it,
                    resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_start),
                    resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_end)))
            recycler_view.itemAnimator = DefaultItemAnimator()
            recycler_view.adapter = adapter

            viewModel.getChatRooms().observe(viewLifecycleOwner, Observer { rooms ->
                rooms?.let {
                    Timber.d("Got items: $it")
                    adapter.values = it
                }
            })

            viewModel.getStatus().observe(viewLifecycleOwner, Observer { status ->
                status?.let { showConnectionState(status) }
            })

            updateSort()
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // TODO - simplify this
            R.id.action_sort -> {
                val dialogLayout = layoutInflater.inflate(R.layout.chatroom_sort_dialog, null)
                val sortType = SharedPreferenceHelper.getInt(Constants.CHATROOM_SORT_TYPE_KEY, ChatRoomsSortOrder.ACTIVITY)
                val groupByType = SharedPreferenceHelper.getBoolean(Constants.CHATROOM_GROUP_BY_TYPE_KEY, false)

                val radioGroup = dialogLayout.findViewById<RadioGroup>(R.id.radio_group_sort)
                val groupByTypeCheckBox = dialogLayout.findViewById<CheckBox>(R.id.checkbox_group_by_type)

                radioGroup.check(when (sortType) {
                    0 -> R.id.radio_sort_alphabetical
                    else -> R.id.radio_sort_activity
                })
                radioGroup.setOnCheckedChangeListener({ _, checkedId ->
                    run {
                        SharedPreferenceHelper.putInt(Constants.CHATROOM_SORT_TYPE_KEY, when (checkedId) {
                            R.id.radio_sort_alphabetical -> 0
                            R.id.radio_sort_activity -> 1
                            else -> 1
                        })
                    }
                })

                groupByTypeCheckBox.isChecked = groupByType
                groupByTypeCheckBox.setOnCheckedChangeListener({ _, isChecked ->
                    SharedPreferenceHelper.putBoolean(Constants.CHATROOM_GROUP_BY_TYPE_KEY, isChecked)
                })

                val dialogSort = AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_sort_title)
                    .setView(dialogLayout)
                    .setPositiveButton("Done", { dialog, _ ->
                        invalidateQueryOnSearch()
                        updateSort()
                        dialog.dismiss()
                    })

                dialogSort.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateSort() {
        val sortType = SharedPreferenceHelper.getInt(Constants.CHATROOM_SORT_TYPE_KEY, ChatRoomsSortOrder.ACTIVITY)
        val grouped = SharedPreferenceHelper.getBoolean(Constants.CHATROOM_GROUP_BY_TYPE_KEY, false)

        val order = when(sortType) {
            ChatRoomsSortOrder.ALPHABETICAL -> {
                if (grouped) {
                    ChatRoomsRepository.Order.GROUPED_NAME
                } else {
                    ChatRoomsRepository.Order.NAME
                }
            }
            ChatRoomsSortOrder.ACTIVITY -> {
                if (grouped) {
                    ChatRoomsRepository.Order.GROUPED_ACTIVITY
                } else {
                    ChatRoomsRepository.Order.ACTIVITY
                }
            }
            else -> ChatRoomsRepository.Order.ACTIVITY
        }

        viewModel.setOrdering(order)
    }

    private fun invalidateQueryOnSearch() {
        searchView?.let {
            if (!searchView!!.isIconified) {
                queryChatRoomsByName(searchView!!.query.toString())
            }
        }
    }

    override suspend fun updateChatRooms(newDataSet: List<ChatRoom>) {}

    override fun showNoChatRoomsToDisplay() {
        ui { text_no_data_to_display.setVisible(true) }
    }

    override fun showLoading() {
        ui { view_loading.setVisible(true) }
    }

    override fun hideLoading() {
        ui {
            view_loading.setVisible(false)
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

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun showConnectionState(state: State) {
        Timber.d("Got new state: $state")
        ui {
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
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.title_chats)
    }

    private fun queryChatRoomsByName(name: String?): Boolean {
        //presenter.chatRoomsByName(name ?: "")
        return true
    }
}