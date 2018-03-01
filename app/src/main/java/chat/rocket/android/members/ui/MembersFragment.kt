package chat.rocket.android.members.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.members.adapter.MembersAdapter
import chat.rocket.android.members.presentation.MembersPresenter
import chat.rocket.android.members.presentation.MembersView
import chat.rocket.android.members.viewmodel.MemberViewModel
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.common.model.User
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_members.*
import javax.inject.Inject

fun newInstance(chatRoomId: String, chatRoomType: String): Fragment {
    return MembersFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_CHAT_ROOM_TYPE = "chat_room_type"

class MembersFragment : Fragment(), MembersView {
    @Inject lateinit var presenter: MembersPresenter
    private lateinit var adapter: MembersAdapter

    private lateinit var chatRoomId: String
    private lateinit var chatRoomType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomType = bundle.getString(BUNDLE_CHAT_ROOM_TYPE)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_members)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
        presenter.loadChatRoomsMembers(chatRoomId, chatRoomType)
    }

    override fun showMembers(dataSet: List<MemberViewModel>, total: Long) {
        activity?.apply {
            setupToolbar(total)
            if (recycler_view.adapter == null) {
                adapter = MembersAdapter(dataSet)
                recycler_view.adapter = adapter
                val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                recycler_view.layoutManager = linearLayoutManager
                recycler_view.addItemDecoration(DividerItemDecoration(this))
                if (dataSet.size >= 30) {
                    recycler_view.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(page: Int, totalItemsCount: Int, recyclerView: RecyclerView?) {
                            presenter.loadChatRoomsMembers(chatRoomId, chatRoomType, page * 30L)
                        }
                    })
                }
            } else {
                adapter.appendData(dataSet)
            }
        }
    }

    override fun showLoading() = view_loading.setVisible(true)

    override fun hideLoading() = view_loading.setVisible(false)

    override fun showMessage(resId: Int) = showToast(resId)

    override fun showMessage(message: String) = showToast(message)

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    private fun setupToolbar(totalMembers: Long) {
        (activity as ChatRoomActivity).setupToolbarTitle(getString(R.string.title_members, totalMembers))
    }
}