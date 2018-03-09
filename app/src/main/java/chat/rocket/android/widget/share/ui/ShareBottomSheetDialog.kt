package chat.rocket.android.widget.share.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.main.presentation.ChatRoomsViewModel
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.widget.share.presentation.SharePresenter
import chat.rocket.android.widget.share.presentation.ShareView
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.bottom_sheet_share.*
import java.util.*
import javax.inject.Inject

class ShareBottomSheetDialog : BottomSheetDialogFragment(), ShareView {
    @Inject lateinit var presenter: SharePresenter
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var serverInteractor: GetCurrentServerInteractor
    private lateinit var adapter: ShareRoomAdapter
    private lateinit var chatRoomsViewModel: ChatRoomsViewModel
    private var contentToShare: Any? = null

    companion object {
        const val ARGUMENT_SHARED_CONTENT = "ARGUMENT_SHARED_CONTENT"
        fun newInstance(uri: Uri): ShareBottomSheetDialog {
            return ShareBottomSheetDialog().apply {
                val args = Bundle()
                args.putParcelable(ARGUMENT_SHARED_CONTENT, uri)
                arguments = args
            }
        }

        fun newInstance(uris: ArrayList<Uri>): ShareBottomSheetDialog {
            return ShareBottomSheetDialog().apply {
                val args = Bundle()
                args.putParcelableArrayList(ARGUMENT_SHARED_CONTENT, uris)
                arguments = args
            }
        }

        fun newInstance(text: String): ShareBottomSheetDialog {
            return ShareBottomSheetDialog().apply {
                val args = Bundle()
                args.putString(ARGUMENT_SHARED_CONTENT, text)
                arguments = args
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottom_sheet_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ShareRoomAdapter(settingsRepository.get(serverInteractor.get()!!)!!, { chatRoom, content ->
            chatRoomsViewModel.selectChatRoom(chatRoom, content)
            this.dismiss()
        })
        val layoutManager = GridLayoutManager(view.context, 4)
        recycler_share.layoutManager = layoutManager
        recycler_share.itemAnimator = DefaultItemAnimator()
        recycler_share.adapter = adapter
        chatRoomsViewModel.getChatRooms().observe(this, Observer { chatRooms ->
            chatRooms?.let {
                adapter.updateRooms(it, contentToShare)
                chatRoomsViewModel.getChatRooms().removeObservers(this)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        arguments?.let {
            contentToShare = it.get(ARGUMENT_SHARED_CONTENT)
        }
        chatRoomsViewModel = ViewModelProviders.of(activity!!).get(ChatRoomsViewModel::class.java)
    }

    override fun shareToRoom(roomId: String, uri: Uri, name: String) {

    }

    override fun shareToRoom(roomId: String, content: String) {

    }

    override fun showRoomsForSharing(rooms: List<String>) {

    }
}