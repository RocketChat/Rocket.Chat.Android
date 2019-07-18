package chat.rocket.android.chatrooms.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.domain.model.DeepLinkInfo
import chat.rocket.android.chatrooms.adapter.RoomsAdapter
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.chatrooms.presentation.ChatRoomsPresenter
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.chatrooms.viewmodel.ChatRoomsViewModel
import chat.rocket.android.chatrooms.viewmodel.ChatRoomsViewModelFactory
import chat.rocket.android.chatrooms.viewmodel.LoadingState
import chat.rocket.android.chatrooms.viewmodel.Query
import chat.rocket.android.servers.ui.ServersBottomSheetFragment
import chat.rocket.android.sortingandgrouping.ui.SortingAndGroupingBottomSheetFragment
import chat.rocket.android.util.extension.onQueryTextListener
import chat.rocket.android.util.extensions.fadeIn
import chat.rocket.android.util.extensions.fadeOut
import chat.rocket.android.util.extensions.ifNotNullNotEmpty
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.core.internal.realtime.socket.model.State
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar_chat_rooms.*
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import javax.inject.Inject

internal const val TAG_CHAT_ROOMS_FRAGMENT = "ChatRoomsFragment"

private const val BUNDLE_CHAT_ROOM_ID = "BUNDLE_CHAT_ROOM_ID"

fun newInstance(chatRoomId: String?, deepLinkInfo: DeepLinkInfo?): Fragment =
    ChatRoomsFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putParcelable(
                chat.rocket.android.authentication.domain.model.DEEP_LINK_INFO_KEY,
                deepLinkInfo
            )
        }
    }

class ChatRoomsFragment : Fragment(), ChatRoomsView {
    @Inject lateinit var presenter: ChatRoomsPresenter
    @Inject lateinit var factory: ChatRoomsViewModelFactory
    @Inject lateinit var analyticsManager: AnalyticsManager
    private lateinit var viewModel: ChatRoomsViewModel
    private var chatRoomId: String? = null
    private var deepLinkInfo: DeepLinkInfo? = null

    private var isSortByName = false
    private var isUnreadOnTop = false
    private var isGroupByType = false
    private var isGroupByFavorites = false

    private val handler = Handler()
    private val dismissConnectionState by lazy { text_connection_status.fadeOut() }
    private var lastConnectionState: State? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            chatRoomId = getString(BUNDLE_CHAT_ROOM_ID)

            chatRoomId.ifNotNullNotEmpty {
                presenter.loadChatRoom(it)
                chatRoomId = null
            }
            deepLinkInfo =
                getParcelable(chat.rocket.android.authentication.domain.model.DEEP_LINK_INFO_KEY)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_chat_rooms)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(presenter) {
            getCurrentServerName()
            getSortingAndGroupingPreferences()
        }

        viewModel = ViewModelProviders.of(this, factory).get(ChatRoomsViewModel::class.java)
        subscribeUi()

        deepLinkInfo?.let {
            processDeepLink(it)
        }
        deepLinkInfo = null

        setupListeners()

        analyticsManager.logScreenView(ScreenViewEvent.ChatRooms)
    }

    override fun setupToolbar(serverName: String) {
        with((activity as AppCompatActivity)) {
            with(toolbar) {
                setSupportActionBar(this)
                supportActionBar?.setDisplayShowTitleEnabled(false)
                setNavigationOnClickListener { presenter.toSettings() }
            }
        }
        text_server_name.text = serverName
    }

    override fun setupSortingAndGrouping(
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    ) {
        this.isSortByName = isSortByName
        this.isUnreadOnTop = isUnreadOnTop
        this.isGroupByType = isGroupByType
        this.isGroupByFavorites = isGroupByFavorites

        if (isSortByName) {
            text_sort_by.text =
                getString(
                    R.string.msg_sort_by_placeholder,
                    getString(R.string.msg_sort_by_name).toLowerCase()
                )
        } else {
            text_sort_by.text = getString(
                R.string.msg_sort_by_placeholder,
                getString(R.string.msg_sort_by_activity).toLowerCase()
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chatrooms, menu)

        val searchMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchMenuItem?.actionView as SearchView

        with(searchView) {
            setIconifiedByDefault(false)
            maxWidth = Integer.MAX_VALUE
            onQueryTextListener { queryChatRoomsByName(it) }
        }

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // We need to show all the menu items here by invalidating the options to recreate the entire menu.
                activity?.invalidateOptionsMenu()
                queryChatRoomsByName(null)
                hideDirectoryView()
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // We need to hide the all the menu items here.
                menu.findItem(R.id.action_new_channel).isVisible = false
                showDirectoryView()
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_channel -> presenter.toCreateChannel()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNoChatRoomsToDisplay() {
//        ui { text_no_data_to_display.isVisible = true }
    }

    override fun showLoading() {
        ui {
            view_loading.isVisible = true
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.isVisible = false
        }
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() {
        ui {
            showMessage(getString(R.string.msg_generic_error))
        }
    }

    private fun showConnectionState(state: State) {
        ui {
            if (state != lastConnectionState) {
                text_connection_status.fadeIn()
                handler.removeCallbacks { dismissConnectionState }
                text_connection_status.text = when (state) {
                    is State.Connected -> {
                        handler.postDelayed({ dismissConnectionState }, 2000)
                        getString(R.string.status_connected)
                    }
                    is State.Disconnected -> getString(R.string.status_disconnected)
                    is State.Connecting -> getString(R.string.status_connecting)
                    is State.Authenticating -> getString(R.string.status_authenticating)
                    is State.Disconnecting -> getString(R.string.status_disconnecting)
                    is State.Waiting -> getString(R.string.status_waiting, state.seconds)
                    is State.Created -> "" // Show nothing
                }
                lastConnectionState = state
            }
        }
    }

    private fun subscribeUi() {
        ui {
            val adapter = RoomsAdapter { room ->
                presenter.loadChatRoom(room)
            }

            with(recycler_view) {
                layoutManager = LinearLayoutManager(it)
                addItemDecoration(
                    DividerItemDecoration(
                        it,
                        resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_start),
                        resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_end)
                    )
                )
                itemAnimator = DefaultItemAnimator()
            }

            viewModel.getChatRooms().observe(viewLifecycleOwner, Observer { rooms ->
                rooms?.let {
                    adapter.values = it
                    if (recycler_view.adapter != adapter) {
                        recycler_view.adapter = adapter
                    }
                    if (rooms.isNotEmpty()) {
//                        text_no_data_to_display.isVisible = false
                    }
                }
            })

            viewModel.loadingState.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    is LoadingState.Loading -> if (state.count == 0L) showLoading()
                    is LoadingState.Loaded -> {
                        hideLoading()
                        if (state.count == 0L) showNoChatRoomsToDisplay()
                    }
                    is LoadingState.Error -> {
                        hideLoading()
                        showGenericErrorMessage()
                    }
                    is LoadingState.AuthError -> {
                        hideLoading()
                        showMessage(R.string.msg_invalid_session)
                    }
                }
            })

            viewModel.getStatus().observe(viewLifecycleOwner, Observer {
                showConnectionState(it)
            })

            showAllChats()
        }
    }

    private fun setupListeners() {
        if (getString(R.string.server_url).isEmpty()) {
            text_server_name.setOnClickListener {
                ServersBottomSheetFragment().show(
                    activity?.supportFragmentManager,
                    chat.rocket.android.servers.ui.TAG
                )
            }
        }

        text_sort_by.setOnClickListener {
            SortingAndGroupingBottomSheetFragment().show(
                activity?.supportFragmentManager,
                chat.rocket.android.sortingandgrouping.ui.TAG
            )
        }

        text_directory.setOnClickListener { presenter.toDirectory() }
    }

    fun sortChatRoomsList(
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    ) {
        this.isSortByName = isSortByName
        this.isUnreadOnTop = isUnreadOnTop
        this.isGroupByType = isGroupByType
        this.isGroupByFavorites = isGroupByFavorites

        if (isSortByName) {
            viewModel.setQuery(Query.ByName(isGroupByType, isUnreadOnTop))
            changeSortByTitle(getString(R.string.msg_sort_by_name))
        } else {
            viewModel.setQuery(Query.ByActivity(isGroupByType, isUnreadOnTop))
            changeSortByTitle(getString(R.string.msg_sort_by_activity))
        }
    }

    private fun changeSortByTitle(text: String) {
        text_sort_by.text = getString(R.string.msg_sort_by_placeholder, text.toLowerCase())
    }

    private fun queryChatRoomsByName(name: String?): Boolean {
        if (name.isNullOrEmpty()) {
            showAllChats()
        } else {
            viewModel.setQuery(Query.Search(name))
        }
        return true
    }

    fun processDeepLink(deepLinkInfo: DeepLinkInfo) {
        val username = deepLinkInfo.roomName
        username.ifNotNullNotEmpty {
            val localRooms = viewModel.getUsersRoomListLocal(username!!)
            val filteredLocalRooms =
                localRooms.filter { itemHolder -> itemHolder.data is RoomUiModel && (itemHolder.data as RoomUiModel).username == username }

            if (filteredLocalRooms.isNotEmpty()) {
                presenter.loadChatRoom(filteredLocalRooms.first().data as RoomUiModel)
            } else {
                loadRoomFromSpotlight(username)
            }
        }
    }

    private fun loadRoomFromSpotlight(username: String) {
        //check from spotlight when connected
        val statusLiveData = viewModel.getStatus()
        statusLiveData.observe(viewLifecycleOwner, object : Observer<State> {
            override fun onChanged(status: State?) {
                if (status is State.Connected) {
                    val rooms = viewModel.getUsersRoomListSpotlight(username)
                    val filteredRooms =
                        rooms?.filter { itemHolder -> itemHolder.data is RoomUiModel && (itemHolder.data as RoomUiModel).username == username }

                    filteredRooms?.let {
                        if (filteredRooms.isNotEmpty()) {
                            presenter.loadChatRoom(filteredRooms.first().data as RoomUiModel)
                        }
                    }
                    statusLiveData.removeObserver(this)
                }
            }
        })
    }

    private fun showAllChats() {
        if (isSortByName) {
            viewModel.setQuery(Query.ByName(isGroupByType, isUnreadOnTop))
        } else {
            viewModel.setQuery(Query.ByActivity(isGroupByType, isUnreadOnTop))
        }
    }

    private fun showDirectoryView() {
        text_directory.isVisible = true
        text_sort_by.isGone = true
    }

    private fun hideDirectoryView() {
        text_directory.isGone = true
        text_sort_by.isVisible = true
    }
}

