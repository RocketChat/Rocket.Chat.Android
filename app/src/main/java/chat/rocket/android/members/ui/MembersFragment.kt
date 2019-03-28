package chat.rocket.android.members.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.members.adapter.MembersAdapter
import chat.rocket.android.members.presentation.MembersPresenter
import chat.rocket.android.members.presentation.MembersView
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.util.extensions.clearLightStatusBar
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import kotlinx.android.synthetic.main.fragment_members.*
import javax.inject.Inject

fun newInstance(chatRoomId: String): Fragment = MembersFragment().apply {
    arguments = Bundle(1).apply {
        putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
    }
}

internal const val TAG_MEMBERS_FRAGMENT = "MembersFragment"
private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"

class MembersFragment : Fragment(), MembersView {
    @Inject
    lateinit var presenter: MembersPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private val adapter: MembersAdapter =
        MembersAdapter { memberUiModel -> presenter.toMemberDetails(memberUiModel) }
    private val linearLayoutManager = LinearLayoutManager(context)
    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            chatRoomId = getString(BUNDLE_CHAT_ROOM_ID, "")
        } ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_members)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        presenter.loadChatRoomsMembers(chatRoomId)

        analyticsManager.logScreenView(ScreenViewEvent.Members)
    }

    override fun showMembers(dataSet: List<MemberUiModel>, total: Long) {
        ui {
            setupToolbar(total)
            if (adapter.itemCount == 0) {
                adapter.prependData(dataSet)
                if (dataSet.size >= 59) { // TODO Check why the API returns the specified count -1
                    recycler_view.addOnScrollListener(object :
                        EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            recyclerView: RecyclerView
                        ) {
                            presenter.loadChatRoomsMembers(chatRoomId)
                        }
                    })
                }
            } else {
                adapter.appendData(dataSet)
            }
        }
    }

    override fun showLoading() {
        ui { view_loading.isVisible = true }
    }

    override fun hideLoading() {
        ui { view_loading.isVisible = false }
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

    private fun setupRecyclerView() {
        ui {
            recycler_view.layoutManager = LinearLayoutManager(context)
            recycler_view.addItemDecoration(
                DividerItemDecoration(
                    it,
                    DividerItemDecoration.HORIZONTAL
                )
            )
            recycler_view.adapter = adapter
        }
    }

    private fun setupToolbar(totalMembers: Long? = null) {
        with((activity as ChatRoomActivity)) {
            if (totalMembers != null) {
                setupToolbarTitle((getString(R.string.title_counted_members, totalMembers)))
            } else {
                setupToolbarTitle((getString(R.string.title_members)))
            }
            this.clearLightStatusBar()
            toolbar.isVisible = true
        }
    }
}