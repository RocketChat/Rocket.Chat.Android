package chat.rocket.android.favoritemessages.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.ChatRoomAdapter
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.viewmodel.BaseViewModel
import chat.rocket.android.favoritemessages.presentation.FavoriteMessagesPresenter
import chat.rocket.android.favoritemessages.presentation.FavoriteMessagesView
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_favorite_messages.*
import javax.inject.Inject

fun newInstance(chatRoomId: String, chatRoomType: String): Fragment {
    return FavoriteMessagesFragment().apply {
        arguments = Bundle(1).apply {
            putString(INTENT_CHAT_ROOM_ID, chatRoomId)
            putString(INTENT_CHAT_ROOM_TYPE, chatRoomType)
        }
    }
}

private const val INTENT_CHAT_ROOM_ID = "chat_room_id"
private const val INTENT_CHAT_ROOM_TYPE = "chat_room_type"

class FavoriteMessagesFragment : Fragment(), FavoriteMessagesView {
    private lateinit var chatRoomId: String
    private lateinit var chatRoomType: String
    private lateinit var adapter: ChatRoomAdapter
    @Inject
    lateinit var presenter: FavoriteMessagesPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(INTENT_CHAT_ROOM_ID)
            chatRoomType = bundle.getString(INTENT_CHAT_ROOM_TYPE)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
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
    }

    override fun showFavoriteMessages(favoriteMessages: List<BaseViewModel<*>>) {
        ui {
            if (recycler_view.adapter == null) {
                adapter = ChatRoomAdapter(chatRoomType, "", null, false)
                recycler_view.adapter = adapter
                val linearLayoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                recycler_view.layoutManager = linearLayoutManager
                recycler_view.itemAnimator = DefaultItemAnimator()
                if (favoriteMessages.size > 10) {
                    recycler_view.addOnScrollListener(object :
                        EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            recyclerView: RecyclerView?
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