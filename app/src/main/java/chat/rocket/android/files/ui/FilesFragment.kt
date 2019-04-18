package chat.rocket.android.files.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.files.adapter.FilesAdapter
import chat.rocket.android.files.presentation.FilesPresenter
import chat.rocket.android.files.presentation.FilesView
import chat.rocket.android.files.uimodel.FileUiModel
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.helper.ImageHelper
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_files.*
import javax.inject.Inject

fun newInstance(chatRoomId: String): Fragment = FilesFragment().apply {
    arguments = Bundle(1).apply {
        putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
    }
}

internal const val TAG_FILES_FRAGMENT = "FilesFragment"
private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"

class FilesFragment : Fragment(), FilesView {
    @Inject
    lateinit var presenter: FilesPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private val adapter: FilesAdapter =
        FilesAdapter { fileUiModel -> presenter.openFile(fileUiModel) }
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
    ): View? = container?.inflate(R.layout.fragment_files)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        presenter.loadFiles(chatRoomId)

        analyticsManager.logScreenView(ScreenViewEvent.Files)
    }

    override fun showFiles(dataSet: List<FileUiModel>, total: Long) {
        ui {
            setupToolbar(total)
            if (adapter.itemCount == 0) {
                adapter.prependData(dataSet)
                if (dataSet.size >= 30) {
                    recycler_view.addOnScrollListener(object :
                        EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            recyclerView: RecyclerView
                        ) {
                            presenter.loadFiles(chatRoomId)
                        }
                    })
                }
                group_no_file.isVisible = dataSet.isEmpty()
            } else {
                adapter.appendData(dataSet)
            }
        }
    }

    override fun playMedia(url: String) {
        ui {
            PlayerActivity.play(it, url)
        }
    }

    override fun openImage(url: String, name: String) {
        ui {
            ImageHelper.openImage(root_layout.context, url, name)
        }
    }

    override fun openDocument(uri: Uri) {
        ui {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
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

    override fun showGenericErrorMessage() {
        showMessage(getString(R.string.msg_generic_error))
    }

    override fun showLoading() {
        ui { view_loading.isVisible = true }
    }

    override fun hideLoading() {
        ui { view_loading.isVisible = false }
    }

    private fun setupRecyclerView() {
        ui {
            recycler_view.layoutManager = linearLayoutManager
            recycler_view.addItemDecoration(DividerItemDecoration(it, HORIZONTAL))
            recycler_view.adapter = adapter
        }
    }

    private fun setupToolbar(totalFiles: Long) {
        (activity as ChatRoomActivity).setupToolbarTitle(
            (getString(
                R.string.title_files_total,
                totalFiles
            ))
        )
    }
}