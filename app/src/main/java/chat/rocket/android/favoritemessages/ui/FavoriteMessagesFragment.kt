package chat.rocket.android.favoritemessages.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.chatroom.adapter.ChatRoomAdapter
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.uimodel.BaseUiModel
import chat.rocket.android.favoritemessages.presentation.FavoriteMessagesPresenter
import chat.rocket.android.favoritemessages.presentation.FavoriteMessagesView
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_favorite_messages.*
import javax.inject.Inject

fun newInstance(chatRoomId: String): Fragment = FavoriteMessagesFragment().apply {
    arguments = Bundle(1).apply {
        putString(INTENT_CHAT_ROOM_ID, chatRoomId)
    }
}

internal const val TAG_FAVORITE_MESSAGES_FRAGMENT = "FavoriteMessagesFragment"
private const val INTENT_CHAT_ROOM_ID = "chat_room_id"

class FavoriteMessagesFragment : Fragment(), FavoriteMessagesView {
    @Inject
    lateinit var presenter: FavoriteMessagesPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private lateinit var chatRoomId: String
    private val adapter = ChatRoomAdapter(enableActions = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            chatRoomId = getString(INTENT_CHAT_ROOM_ID, "")
        } ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_favorite_messages)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        presenter.loadFavoriteMessages(chatRoomId)

        analyticsManager.logScreenView(ScreenViewEvent.FavoriteMessages)
    }

    override fun showFavoriteMessages(favoriteMessages: List<BaseUiModel<*>>) {
        ui {
            if (recycler_view.adapter == null) {
                recycler_view.adapter = adapter
                val linearLayoutManager = LinearLayoutManager(context)
                recycler_view.layoutManager = linearLayoutManager
                recycler_view.itemAnimator = DefaultItemAnimator()
                if (favoriteMessages.size >= 30) {
                    recycler_view.addOnScrollListener(object :
                        EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            recyclerView: RecyclerView
                        ) {
                            presenter.loadFavoriteMessages(chatRoomId)
                        }

                    })
                }
                no_messages_view.isVisible = favoriteMessages.isEmpty()
            }
            adapter.appendData(favoriteMessages)
        }
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun showLoading() {
        ui { view_loading.isVisible = true }
    }

    override fun hideLoading() {
        ui { view_loading.isVisible = false }
    }

    private fun setupToolbar() {
        (activity as ChatRoomActivity).setupToolbarTitle(getString(R.string.title_favorite_messages))
    }
}