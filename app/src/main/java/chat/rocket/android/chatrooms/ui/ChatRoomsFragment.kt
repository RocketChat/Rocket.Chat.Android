package chat.rocket.android.chatrooms.ui

import androidx.appcompat.app.AlertDialog
import android.app.ProgressDialog
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
import chat.rocket.android.contacts.ui.ContactsFragment
import chat.rocket.android.helper.ChatRoomsSortOrder
import chat.rocket.android.helper.Constants
import chat.rocket.android.helper.SharedPreferenceHelper
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.util.extension.onQueryTextListener
import chat.rocket.android.util.extensions.fadeIn
import chat.rocket.android.util.extensions.fadeOut
import chat.rocket.android.util.extensions.ifNotNullNorEmpty
import chat.rocket.android.util.extensions.ifNotNullNotEmpty
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.core.internal.realtime.socket.model.State
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import timber.log.Timber
import javax.inject.Inject

// WIDECHAT
import android.graphics.Color
import android.widget.*
import androidx.core.view.isGone
import chat.rocket.android.authentication.domain.model.DeepLinkInfo
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.profile.ui.ProfileFragment
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.settings.ui.SettingsFragment
import chat.rocket.android.util.extensions.avatarUrl
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.coroutines.experimental.launch

internal const val TAG_CHAT_ROOMS_FRAGMENT = "ChatRoomsFragment"

private const val BUNDLE_CHAT_ROOM_ID = "BUNDLE_CHAT_ROOM_ID"

class ChatRoomsFragment : Fragment(), ChatRoomsView {
    @Inject
    lateinit var presenter: ChatRoomsPresenter
    @Inject
    lateinit var factory: ChatRoomsViewModelFactory
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    // WIDECHAT
    @Inject
    lateinit var serverInteractor: GetCurrentServerInteractor
    @Inject
    lateinit var userHelper: UserHelper
    // END WIDECHAT

    private lateinit var viewModel: ChatRoomsViewModel

    private var searchView: SearchView? = null
    private var sortView: MenuItem? = null
    private val handler = Handler()
    private var chatRoomId: String? = null
    private var progressDialog: ProgressDialog? = null

    // WIDECHAT
    private var settingsView: MenuItem? = null
    private var searchIcon: ImageView? = null
    private var searchText:  TextView? = null
    private var searchCloseButton: ImageView? = null
    private var profileButton: SimpleDraweeView? = null
    private var currentUserStatusIcon: ImageView? = null
    private var deepLinkInfo: DeepLinkInfo? = null
    // handles that recurring connection status bug in widechat
    private var currentlyConnected: Boolean? = false

    companion object {
        fun newInstance(chatRoomId: String? = null, deepLinkInfo: DeepLinkInfo? = null): ChatRoomsFragment {
            return ChatRoomsFragment().apply {
                arguments = Bundle(1).apply {
                    putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
                    putParcelable(Constants.DEEP_LINK_INFO, deepLinkInfo)
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
            chatRoomId.ifNotNullNotEmpty { roomId ->
                presenter.loadChatRoom(roomId)
                chatRoomId = null
            }
            deepLinkInfo = bundle.getParcelable<DeepLinkInfo>(Constants.DEEP_LINK_INFO)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(dismissStatus)
        super.onDestroy()
    }

    override fun onResume() {
        // WIDECHAT - cleanup any titles set by other fragments; clear any previous search
        if (Constants.WIDECHAT) {
            widechat_welcome_to_app.isVisible = false
            widechat_text_no_data_to_display.isVisible = false
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayShowTitleEnabled(false)
            searchView?.clearFocus()
            searchView?.setQuery("", false)
            viewModel.showLastMessage = true
        }
        setCurrentUserStatusIcon()
        super.onResume()
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
        setupFab()
        deepLinkInfo?.let {
            processDeepLink(it)
        }
        deepLinkInfo = null

        analyticsManager.logScreenView(ScreenViewEvent.ChatRooms)
    }

    private fun subscribeUi() {
        ui {
            val adapter = RoomsAdapter { room ->
                presenter.loadChatRoom(room)
            }

            recycler_view.layoutManager = LinearLayoutManager(it)
            recycler_view.addItemDecoration(
                DividerItemDecoration(
                    it,
                    resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_start),
                    resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_end)
                )
            )
            recycler_view.itemAnimator = DefaultItemAnimator()
            recycler_view.adapter = adapter

            viewModel.getChatRooms().observe(viewLifecycleOwner, Observer { rooms ->
                rooms?.let {
                    Timber.d("Got items: $it")
                    adapter.values = it
                    if (rooms.isNotEmpty()) {
                        showNoChatRoomsToDisplay(false)
                    }
                }
            })

            viewModel.loadingState.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    is LoadingState.Loading -> if (state.count == 0L) showLoading()
                    is LoadingState.Loaded -> {
                        if (state.count == 0L) showNoChatRoomsToDisplay(true)
                    }
                    is LoadingState.Error -> {
                        showGenericErrorMessage()
                    }
                }
            })

            viewModel.getStatus().observe(viewLifecycleOwner, Observer { status ->
                if (Constants.WIDECHAT) {
                    if (status is State.Connected) {
                        // When connected, only show the connection status once
                        if (currentlyConnected == false) {
                            currentlyConnected = true
                            status?.let {showConnectionState(status)}
                        }
                    } else {
                        currentlyConnected = false
                        status?.let {showConnectionState(status)}
                    }
                } else {
                    status?.let { showConnectionState(status) }
                }
            })
            updateSort()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        if (Constants.WIDECHAT) {
            inflater.inflate(R.menu.widechat_chatrooms, menu)
            settingsView = menu.findItem(R.id.action_settings)
            settingsView?.isVisible = true
            return
        }

        inflater.inflate(R.menu.chatrooms, menu)
        sortView = menu.findItem(R.id.action_sort)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as? SearchView
        searchView?.setIconifiedByDefault(false)
        searchView?.maxWidth = Integer.MAX_VALUE
        searchView?.onQueryTextListener { queryChatRoomsByName(it) }

        val expandListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Simply setting sortView to visible won't work, so we invalidate the options
                // to recreate the entire menu...
                viewModel.showLastMessage = true
                activity?.invalidateOptionsMenu()
                create_new_channel_fab.isVisible = true
                queryChatRoomsByName(null)
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                viewModel.showLastMessage = false
                sortView?.isVisible = false
                create_new_channel_fab.isVisible = false
                return true
            }
        }
        searchItem?.setOnActionExpandListener(expandListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // TODO - simplify this
            R.id.action_sort -> {
                val dialogLayout = layoutInflater.inflate(R.layout.chatroom_sort_dialog, null)
                val sortType = SharedPreferenceHelper.getInt(
                    Constants.CHATROOM_SORT_TYPE_KEY,
                    ChatRoomsSortOrder.ACTIVITY
                )
                val groupByType =
                    SharedPreferenceHelper.getBoolean(Constants.CHATROOM_GROUP_BY_TYPE_KEY, false)

                val radioGroup = dialogLayout.findViewById<RadioGroup>(R.id.radio_group_sort)
                val groupByTypeCheckBox =
                    dialogLayout.findViewById<CheckBox>(R.id.checkbox_group_by_type)

                radioGroup.check(
                    when (sortType) {
                        0 -> R.id.radio_sort_alphabetical
                        else -> R.id.radio_sort_activity
                    }
                )
                radioGroup.setOnCheckedChangeListener { _, checkedId ->
                    run {
                        SharedPreferenceHelper.putInt(
                            Constants.CHATROOM_SORT_TYPE_KEY, when (checkedId) {
                                R.id.radio_sort_alphabetical -> 0
                                R.id.radio_sort_activity -> 1
                                else -> 1
                            }
                        )
                    }
                }

                groupByTypeCheckBox.isChecked = groupByType
                groupByTypeCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    SharedPreferenceHelper.putBoolean(
                        Constants.CHATROOM_GROUP_BY_TYPE_KEY,
                        isChecked
                    )
                }

                context?.let {
                    AlertDialog.Builder(it)
                        .setTitle(R.string.dialog_sort_title)
                        .setView(dialogLayout)
                        .setPositiveButton(R.string.msg_sort) { dialog, _ ->
                            invalidateQueryOnSearch()
                            updateSort()
                            dialog.dismiss()
                        }.show()
                }
            }
        }
        
        if (Constants.WIDECHAT) {
            when (item.itemId) {
                R.id.action_settings -> {
                    searchView?.clearFocus()
                    val newFragment = SettingsFragment()
                    val fragmentManager = fragmentManager
                    val fragmentTransaction = fragmentManager!!.beginTransaction()
                    fragmentTransaction.replace(R.id.fragment_container, newFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateSort() {
        val sortType = SharedPreferenceHelper.getInt(
            Constants.CHATROOM_SORT_TYPE_KEY,
            ChatRoomsSortOrder.ACTIVITY
        )
        val grouped = SharedPreferenceHelper.getBoolean(Constants.CHATROOM_GROUP_BY_TYPE_KEY, false)

        val query = when (sortType) {
            ChatRoomsSortOrder.ALPHABETICAL -> {
                Query.ByName(grouped)
            }
            ChatRoomsSortOrder.ACTIVITY -> {
                Query.ByActivity(grouped)
            }
            else -> Query.ByActivity()
        }

        viewModel.setQuery(query)
    }

    private fun invalidateQueryOnSearch() {
        searchView?.let {
            if (!searchView!!.isIconified) {
                queryChatRoomsByName(searchView!!.query.toString())
            }
        }
    }

    /** WIDECHAT - adjust the view; expand searchView by default;
     *  remove keyboard and query with close button
     */
    private fun setupWidechatSearchView() {
        searchView?.setBackgroundResource(R.drawable.widechat_search_white_background)
        searchView?.isIconified = false

        searchIcon = searchView?.findViewById(R.id.search_mag_icon)
        searchIcon?.setImageResource(R.drawable.ic_search_gray_24px)

        searchText = searchView?.findViewById(R.id.search_src_text)
        searchText?.setTextColor(Color.GRAY)
        searchText?.setHintTextColor(Color.GRAY)
        
        searchText?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                viewModel.showLastMessage = false
        }

        searchCloseButton = searchView?.findViewById(R.id.search_close_btn)
        searchCloseButton?.setImageResource(R.drawable.ic_close_gray_24dp)

        searchCloseButton?.setOnClickListener { v ->
            searchView?.clearFocus()
            searchView?.setQuery("", false)
            viewModel.showLastMessage = true
        }

        searchView?.onQueryTextListener { queryChatRoomsByName(it) }
    }

    private fun showNoChatRoomsToDisplay(show: Boolean) {
        hideLoading()
        if (Constants.WIDECHAT) {
            ui { widechat_welcome_to_app.isVisible = show
                 widechat_text_no_data_to_display.isVisible = show
            }
        } else {
            ui { text_no_data_to_display.isVisible = show }
        }
    }

    override fun showLoading() {
        view_loading.isVisible = true
    }

    override fun hideLoading() {
        view_loading.isVisible = false
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

    override fun showLoadingRoom(name: CharSequence) {
        ui {
            progressDialog = ProgressDialog.show(activity, "Rocket.Chat", "Loading room $name")
        }
    }

    override fun hideLoadingRoom() {
        progressDialog?.dismiss()
    }

    private fun showConnectionState(state: State) {
        Timber.d("Got new state: $state")
        ui {
            text_connection_status.fadeIn()
            handler.removeCallbacks(dismissStatus)
            when (state) {
                is State.Connected -> {
                    text_connection_status.text = getString(R.string.status_connected)
                    handler.postDelayed(dismissStatus, 2000)
                    setCurrentUserStatusIcon()
                }
                is State.Disconnected -> text_connection_status.text =
                        getString(R.string.status_disconnected)
                is State.Connecting -> text_connection_status.text =
                        getString(R.string.status_connecting)
                is State.Authenticating -> text_connection_status.text =
                        getString(R.string.status_authenticating)
                is State.Disconnecting -> text_connection_status.text =
                        getString(R.string.status_disconnecting)
                is State.Waiting -> text_connection_status.text =
                        getString(R.string.status_waiting, state.seconds)
            }
        }
    }

    private val dismissStatus = {
        if (text_connection_status != null) {
            text_connection_status.fadeOut()
        }
    }

    private fun setupToolbar() {
        if (Constants.WIDECHAT) {
            with((activity as MainActivity).toolbar) {
                title = null
                navigationIcon = null
            }

            // WIDECHAT sets custom toolbar with profile button and searchView
            with((activity as AppCompatActivity?)?.supportActionBar) {
                this?.setDisplayShowCustomEnabled(true)
                this?.setDisplayShowTitleEnabled(false)
                this?.setCustomView(R.layout.widechat_search_layout)

                searchView = this?.getCustomView()?.findViewById(R.id.action_widechat_search)
                setupWidechatSearchView()

                val serverUrl = serverInteractor.get()
                val user = userHelper.user()
                val myAvatarUrl: String? =  serverUrl?.avatarUrl(user?.username ?: "")

                profileButton = this?.getCustomView()?.findViewById(R.id.profile_image_avatar)
                profileButton?.setImageURI(myAvatarUrl)
                profileButton?.setOnClickListener { v ->

                    searchView?.clearFocus()
                    val newFragment = ProfileFragment()
                    val fragmentManager = fragmentManager
                    val fragmentTransaction = fragmentManager!!.beginTransaction()
                    fragmentTransaction.replace(R.id.fragment_container, newFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }
            }
        } else {
            (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.title_chats)
            (activity as MainActivity).setupNavigationView()
        }
    }

    private fun setCurrentUserStatusIcon() {
        with((activity as AppCompatActivity?)?.supportActionBar) {
            currentUserStatusIcon = this?.getCustomView()?.findViewById(R.id.self_status)
        }
        launch {
            val currentUser = presenter.getCurrentUser(false)
            val drawable = DrawableHelper.getUserStatusDrawable(currentUser?.status, context!!)
            ui {
                currentUserStatusIcon?.isVisible = true
                currentUserStatusIcon?.setImageDrawable(drawable)
            }
        }
    }

    private fun queryChatRoomsByName(name: String?): Boolean {
        if (name.isNullOrEmpty()) {
            updateSort()
        } else {
            viewModel.setQuery(Query.Search(name!!))
        }
        return true
    }

    private fun setupFab() {
        create_new_channel_fab.setOnClickListener { view ->
            currentUserStatusIcon?.isGone = true
            val contactsFragment = ContactsFragment()
            val transaction = activity?.supportFragmentManager?.beginTransaction();
            transaction?.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            transaction?.replace(this.id, contactsFragment, "contactsFragment");
            transaction?.addToBackStack("contactsFragment")?.commit();
        }
    }

    fun processDeepLink(deepLinkInfo: DeepLinkInfo) {

        val type = deepLinkInfo.roomType
        val name = deepLinkInfo.roomName

        type.ifNotNullNorEmpty {
            name.ifNotNullNorEmpty {
                val localRooms = viewModel.getChatRoomByNameDB(it.toString())
                val filteredLocalRooms = localRooms.filter { itemHolder -> itemHolder.data is RoomUiModel && getCheckString(type!!, itemHolder.data as RoomUiModel) == name }

                if (filteredLocalRooms.isNotEmpty()) {
                    presenter.loadChatRoom(filteredLocalRooms.first().data as RoomUiModel)
                } else {
                    //check from spotlight when connected
                    val statusLiveData = viewModel.getStatus()
                    statusLiveData.observe(viewLifecycleOwner, object : Observer<State> {
                        override fun onChanged(status: State?) {
                            if (status is State.Connected) {
                                val rooms = viewModel.getChatRoomByNameSpotlight(name.toString())
                                val filteredRooms = rooms?.filter { itemHolder -> itemHolder.data is RoomUiModel && getCheckString(type!!, itemHolder.data as RoomUiModel) == name }

                                filteredRooms?.let {
                                    if (filteredRooms.isNotEmpty()) {
                                        presenter.loadChatRoom(filteredRooms.first().data as RoomUiModel)
                                    } else {
                                        Toast.makeText(context, "Room not found or No internet connection", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                statusLiveData.removeObserver(this)
                            }
                        }
                    })
                }
            }
        }
    }

    fun getCheckString(type: String, roomUiModel: RoomUiModel): String? {
        return when (type) {
            "direct" -> roomUiModel.username
            "channel" -> roomUiModel.name.toString()
            "group" -> roomUiModel.name.toString()
            else -> ""
        }
    }
}
