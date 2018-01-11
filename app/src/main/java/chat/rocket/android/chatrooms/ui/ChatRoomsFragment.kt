package chat.rocket.android.chatrooms.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.chatrooms.presentation.ChatRoomsPresenter
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.widget.DividerItemDecoration
import chat.rocket.core.model.ChatRoom
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import javax.inject.Inject

class ChatRoomsFragment : Fragment(), ChatRoomsView {
    @Inject lateinit var presenter: ChatRoomsPresenter

    companion object {
        fun newInstance() = ChatRoomsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_chat_rooms, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.chatRooms()
        floating_search_view.setOnQueryChangeListener { oldQuery, newQuery ->
            if (oldQuery.isNotEmpty() && newQuery.isEmpty()) {
                floating_search_view.clearSuggestions()
            } else {
                floating_search_view.showProgress()
            }
        }
    }

    override fun showChatRooms(dataSet: MutableList<ChatRoom>) {
        activity?.apply {
            recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recycler_view.addItemDecoration(DividerItemDecoration(this, 144, 32))
            recycler_view.adapter = ChatRoomsAdapter(dataSet, this)
        }
    }

    override fun showLoading() = view_loading.show()

    override fun hideLoading() = view_loading.hide()

    override fun showMessage(message: String) = Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))
}