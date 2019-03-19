package chat.rocket.android.chatdetails.ui

import DrawableHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.chatdetails.domain.ChatDetails
import chat.rocket.android.chatdetails.presentation.ChatDetailsPresenter
import chat.rocket.android.chatdetails.presentation.ChatDetailsView
import chat.rocket.android.chatdetails.viewmodel.ChatDetailsViewModel
import chat.rocket.android.chatdetails.viewmodel.ChatDetailsViewModelFactory
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.server.domain.CurrentServerRepository
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_details.*
import javax.inject.Inject

fun newInstance(
    chatRoomId: String,
    chatRoomType: String,
    isSubscribed: Boolean,
    isFavorite: Boolean,
    disableMenu: Boolean
): ChatDetailsFragment {
    return ChatDetailsFragment().apply {
        arguments = Bundle(5).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
            putBoolean(BUNDLE_IS_SUBSCRIBED, isSubscribed)
            putBoolean(BUNDLE_IS_FAVORITE, isFavorite)
            putBoolean(BUNDLE_DISABLE_MENU, disableMenu)
        }
   }
}

internal const val TAG_CHAT_DETAILS_FRAGMENT = "ChatDetailsFragment"
internal const val MENU_ACTION_FAVORITE_REMOVE_FAVORITE = 1
internal const val MENU_ACTION_VIDEO_CALL = 2

private const val BUNDLE_CHAT_ROOM_ID = "BUNDLE_CHAT_ROOM_ID"
private const val BUNDLE_CHAT_ROOM_TYPE = "BUNDLE_CHAT_ROOM_TYPE"
private const val BUNDLE_IS_SUBSCRIBED = "BUNDLE_IS_SUBSCRIBED"
private const val BUNDLE_IS_FAVORITE = "BUNDLE_IS_FAVORITE"
private const val BUNDLE_DISABLE_MENU = "BUNDLE_DISABLE_MENU"

class ChatDetailsFragment : Fragment(), ChatDetailsView {
    @Inject
    lateinit var presenter: ChatDetailsPresenter
    @Inject
    lateinit var factory: ChatDetailsViewModelFactory
    @Inject
    lateinit var serverUrl: CurrentServerRepository
    @Inject
    lateinit var settings: GetSettingsInteractor
    private var adapter: ChatDetailsAdapter? = null
    private lateinit var viewModel: ChatDetailsViewModel

    internal lateinit var chatRoomId: String
    internal lateinit var chatRoomType: String
    private var isSubscribed: Boolean = true
    internal var isFavorite: Boolean = false
    private var disableMenu: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            chatRoomId = getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomType = getString(BUNDLE_CHAT_ROOM_TYPE)
            isSubscribed = getBoolean(BUNDLE_IS_SUBSCRIBED)
            isFavorite = getBoolean(BUNDLE_IS_FAVORITE)
            disableMenu = getBoolean(BUNDLE_DISABLE_MENU)
        } ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_chat_details)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(ChatDetailsViewModel::class.java)
        setupOptions()
        setupToolbar()
        getDetails()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        setupMenu(menu)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setOnMenuItemClickListener(item)
        return true
    }

    override fun showFavoriteIcon(isFavorite: Boolean) {
        this.isFavorite = isFavorite
        activity?.invalidateOptionsMenu()
    }

    override fun displayDetails(room: ChatDetails) {
        ui {
            val text = room.name
            name.text = text
            bindImage(chatRoomType)
            content_topic.text =
                    if (room.topic.isNullOrEmpty()) getString(R.string.msg_no_topic) else room.topic
            content_announcement.text =
                    if (room.announcement.isNullOrEmpty()) getString(R.string.msg_no_announcement) else room.announcement
            content_description.text =
                    if (room.description.isNullOrEmpty()) getString(R.string.msg_no_description) else room.description
        }
    }

    override fun showGenericErrorMessage() = showMessage(R.string.msg_generic_error)

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

    private fun addOptions(adapter: ChatDetailsAdapter?) {
        adapter?.let {
            if (!disableMenu) {
                it.addOption(getString(R.string.title_files), R.drawable.ic_files_24dp) {
                    presenter.toFiles(chatRoomId!!)
                }
            }

            if (chatRoomType != RoomType.DIRECT_MESSAGE && !disableMenu) {
                it.addOption(getString(R.string.msg_mentions), R.drawable.ic_at_black_20dp) {
                    presenter.toMentions(chatRoomId!!)
                }
                it.addOption(
                    getString(R.string.title_members),
                    R.drawable.ic_people_outline_black_24dp
                ) {
                    presenter.toMembers(chatRoomId!!)
                }
            }

            it.addOption(
                getString(R.string.title_favorite_messages),
                R.drawable.ic_star_border_white_24dp
            ) {
                presenter.toFavorites(chatRoomId!!)
            }
            it.addOption(
                getString(R.string.title_pinned_messages),
                R.drawable.ic_action_message_pin_24dp
            ) {
                presenter.toPinned(chatRoomId!!)
            }
        }
    }

    private fun bindImage(chatRoomType: String) {
        val drawable = when (roomTypeOf(chatRoomType)) {
            is RoomType.Channel -> {
                DrawableHelper.getDrawableFromId(R.drawable.ic_hashtag_black_12dp, context!!)
            }
            is RoomType.PrivateGroup -> {
                DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_12_dp, context!!)
            }
            else -> null
        }

        drawable?.let {
            val wrappedDrawable = DrawableHelper.wrapDrawable(it)
            val mutableDrawable = wrappedDrawable.mutate()
            DrawableHelper.tintDrawable(mutableDrawable, context!!, R.color.colorPrimary)
            DrawableHelper.compoundDrawable(name, mutableDrawable)
        }
    }

    private fun getDetails() {
        if (isSubscribed)
            viewModel.getDetails(chatRoomId!!).observe(viewLifecycleOwner, Observer { details ->
                displayDetails(details)
            })
        else
            presenter.getDetails(chatRoomId!!, chatRoomType!!)
    }

    private fun setupOptions() {
        ui {
            if (options.adapter == null) {
                adapter = ChatDetailsAdapter()
                options.adapter = adapter

                with(options) {
                    layoutManager = LinearLayoutManager(it)
                    addItemDecoration(
                        DividerItemDecoration(
                            it,
                            resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_start),
                            resources.getDimensionPixelSize(R.dimen.divider_item_decorator_bound_end)
                        )
                    )
                    itemAnimator = DefaultItemAnimator()
                    isNestedScrollingEnabled = false
                }
            }
            addOptions(adapter)
        }
    }

    private fun setupToolbar() {
        with((activity as ChatRoomActivity)) {
            hideExpandMoreForToolbar()
            setupToolbarTitle(getString(R.string.title_channel_details))
        }
    }
}