package chat.rocket.android.chatrooms.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.chatrooms.adapter.RoomsAdapter
import chat.rocket.android.chatrooms.presentation.ChatRoomsPresenter
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.chatrooms.viewmodel.ChatRoomsViewModel
import chat.rocket.android.chatrooms.viewmodel.ChatRoomsViewModelFactory
import chat.rocket.android.chatrooms.viewmodel.LoadingState
import chat.rocket.android.chatrooms.viewmodel.Query
import chat.rocket.android.servers.ui.ServersBottomSheetFragment
import chat.rocket.android.sortingandgrouping.ui.SortingAndGroupingBottomSheetFragment
import chat.rocket.android.util.extension.onQueryTextListener
import chat.rocket.android.util.extensions.ifNotNullNotEmpty
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.core.internal.realtime.socket.model.State
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar_chat_rooms.*
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import timber.log.Timber
import javax.inject.Inject

internal const val TAG_CHAT_ROOMS_FRAGMENT = "ChatRoomsFragment"

private const val BUNDLE_CHAT_ROOM_ID = "BUNDLE_CHAT_ROOM_ID"


fun newInstance(chatRoomId: String?): Fragment = ChatRoomsFragment().apply {
    arguments = Bundle(1).apply {
        putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
    }
}

class ChatRoomsFragment : Fragment(), ChatRoomsView {
    @Inject lateinit var presenter: ChatRoomsPresenter
    @Inject lateinit var factory: ChatRoomsViewModelFactory
    @Inject lateinit var analyticsManager: AnalyticsManager
    private val roomsAdapter = RoomsAdapter { presenter.loadChatRoom(it) }
    private lateinit var viewModel: ChatRoomsViewModel
    private var chatRoomId: String? = null
    private var isSortByName = false
    private var isUnreadOnTop = false
    private var isGroupByType = false
    private var isGroupByFavorites = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        arguments?.run {
            chatRoomId = getString(BUNDLE_CHAT_ROOM_ID)

            chatRoomId.ifNotNullNotEmpty {
                presenter.loadChatRoom(it)
                chatRoomId = null
            }
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
        setupRecyclerView()
        setupListeners()

        with(presenter) {
            getCurrentServerName()
            getSortingAndGroupingPreferences()
        }

        viewModel = ViewModelProviders.of(this, factory).get(ChatRoomsViewModel::class.java)
        subscribeUi()
        showAllChats()

        analyticsManager.logScreenView(ScreenViewEvent.ChatRooms)
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
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // We need to hide the all the menu items here.
                menu.findItem(R.id.action_new_channel).isVisible = false
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

    override fun setupToolbar(serverName: String) {
        with((activity as AppCompatActivity)) {
            with(toolbar) {
                setSupportActionBar(this)
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
                getString(R.string.msg_sort_by, getString(R.string.msg_sort_by_name).toLowerCase())
        } else {
            text_sort_by.text = getString(
                R.string.msg_sort_by,
                getString(R.string.msg_sort_by_activity).toLowerCase()
            )
        }
    }

    override fun showLoading() {
        ui {
            view_loading.isVisible = true
        }
    }

    override fun hideLoading() {
        ui { view_loading.isVisible = false }
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    private fun setupRecyclerView() {
        ui {
            with(recycler_view) {
                if (adapter == null) {
                    adapter = roomsAdapter
                }

                layoutManager = LinearLayoutManager(context)
                addItemDecoration(
                    DividerItemDecoration(
                        context,
                        resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_start),
                        resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_end)
                    )
                )
                itemAnimator = DefaultItemAnimator()
            }
        }
    }

    private fun setupListeners() {
        text_server_name.setOnClickListener {
            ServersBottomSheetFragment().show(
                activity?.supportFragmentManager,
                chat.rocket.android.servers.ui.TAG
            )
        }

        text_sort_by.setOnClickListener {
            SortingAndGroupingBottomSheetFragment().show(
                activity?.supportFragmentManager,
                chat.rocket.android.sortingandgrouping.ui.TAG
            )
        }
    }

    private fun subscribeUi() {
        ui {
            viewModel.getChatRooms().observe(viewLifecycleOwner, Observer { roomModel ->
                roomModel?.let { roomsAdapter.values = it }
            })

            viewModel.loadingState.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    is LoadingState.Loading -> showLoading()
                    is LoadingState.Loaded -> hideLoading()
                    is LoadingState.Error -> {
                        hideLoading()
                        showGenericErrorMessage()
                    }
                }
            })

            // Actually it is fetching the rooms. We should fix it.
            viewModel.getStatus().observe(viewLifecycleOwner, Observer { status ->
                status?.let {
                    //showConnectionState(status)
                }
            })
        }
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
            viewModel.setQuery(Query.ByName(isGroupByType))
            changeSortByTitle(getString(R.string.msg_sort_by_name))
        } else {
            viewModel.setQuery(Query.ByActivity(isGroupByType))
            changeSortByTitle(getString(R.string.msg_sort_by_activity))
        }
    }

    private fun changeSortByTitle(text: String) {
        text_sort_by.text = getString(R.string.msg_sort_by, text.toLowerCase())
    }

    private fun queryChatRoomsByName(name: String?): Boolean {
        if (name.isNullOrEmpty()) {
            showAllChats()
        } else {
            viewModel.setQuery(Query.Search(name))
        }
        return true
    }

    private fun showAllChats() {
        if (isSortByName) {
            viewModel.setQuery(Query.ByName(isGroupByType))
        } else {
            viewModel.setQuery(Query.ByActivity(isGroupByType))
        }
    }
}
