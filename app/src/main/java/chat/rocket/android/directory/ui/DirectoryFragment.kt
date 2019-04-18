package chat.rocket.android.directory.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.directory.adapter.DirectoryAdapter
import chat.rocket.android.directory.adapter.Selector
import chat.rocket.android.directory.presentation.DirectoryPresenter
import chat.rocket.android.directory.presentation.DirectoryView
import chat.rocket.android.directory.uimodel.DirectoryUiModel
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_directory.*
import kotlinx.android.synthetic.main.fragment_settings.view_loading
import javax.inject.Inject

internal const val TAG_DIRECTORY_FRAGMENT = "DirectoryFragment"

fun newInstance(): Fragment = DirectoryFragment()

class DirectoryFragment : Fragment(), DirectoryView {
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var presenter: DirectoryPresenter
    private var isSortByChannels: Boolean = true
    private var isSearchForGlobalUsers: Boolean = false
    private val linearLayoutManager = LinearLayoutManager(context)
    private val directoryAdapter = DirectoryAdapter(object : Selector {
        override fun onChannelSelected(channelId: String, channelName: String) {
            presenter.toChannel(channelId, channelName)
        }
        override fun onUserSelected(username: String, name: String) {
            presenter.tiDirectMessage(username, name)
        }
        override fun onGlobalUserSelected(username: String, name: String) {
            presenter.tiDirectMessage(username, name)
        }
    })
    private val hashtagDrawable by lazy {
        DrawableHelper.getDrawableFromId(R.drawable.ic_hashtag_16dp, text_sort_by.context)
    }
    private val userDrawable by lazy {
        DrawableHelper.getDrawableFromId(R.drawable.ic_user_16dp, text_sort_by.context)
    }
    private val arrowDownDrawable by lazy {
        DrawableHelper.getDrawableFromId(R.drawable.ic_arrow_down, text_sort_by.context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_directory)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        presenter.loadAllDirectoryChannels()
        analyticsManager.logScreenView(ScreenViewEvent.Directory)
    }

    override fun showChannels(dataSet: List<DirectoryUiModel>) {
        ui {
            if (directoryAdapter.itemCount == 0) {
                directoryAdapter.prependData(dataSet)
                if (dataSet.size >= 60) {
                    recycler_view.addOnScrollListener(object :
                        EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            recyclerView: RecyclerView
                        ) {
                            presenter.loadAllDirectoryChannels()
                        }
                    })
                }
            } else {
                directoryAdapter.appendData(dataSet)
            }
        }
    }

    override fun showUsers(dataSet: List<DirectoryUiModel>) {
        ui {
            if (directoryAdapter.itemCount == 0) {
                directoryAdapter.prependData(dataSet)
                if (dataSet.size >= 60) {
                    recycler_view.addOnScrollListener(object :
                        EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            recyclerView: RecyclerView
                        ) {
                            presenter.loadAllDirectoryUsers(isSearchForGlobalUsers)
                        }
                    })
                }
            } else {
                directoryAdapter.appendData(dataSet)
            }
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
        view_loading.isVisible = true
    }

    override fun hideLoading() {
        view_loading.isVisible = false
    }

    fun updateSorting(isSortByChannels: Boolean, isSearchForGlobalUsers: Boolean) {
        if (this.isSortByChannels != isSortByChannels ||
            this.isSearchForGlobalUsers != isSearchForGlobalUsers
        ) {
            this.isSortByChannels = isSortByChannels
            this.isSearchForGlobalUsers = isSearchForGlobalUsers
            updateSortByTitle()
            with(directoryAdapter) {
                clearData()
                setSorting(isSortByChannels, isSearchForGlobalUsers)
            }
            presenter.updateSorting(isSortByChannels, isSearchForGlobalUsers)
        }
    }

    private fun setupToolbar() {
        with((activity as AppCompatActivity)) {
            with(toolbar) {
                setSupportActionBar(this)
                title = getString(R.string.msg_directory)
                setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
                setNavigationOnClickListener { activity?.onBackPressed() }
            }
        }
    }

    private fun setupRecyclerView() {
        ui {
            with(recycler_view) {
                layoutManager = linearLayoutManager
                addItemDecoration(DividerItemDecoration(it, DividerItemDecoration.HORIZONTAL))
                adapter = directoryAdapter
            }
        }
    }

    private fun setupListeners() {
        text_sort_by.setOnClickListener {
            activity?.supportFragmentManager?.let {
                showDirectorySortingBottomSheetFragment(isSortByChannels, isSearchForGlobalUsers, it)
            }
        }
    }


    private fun updateSortByTitle() {
        if (isSortByChannels) {
            text_sort_by.text = getString(R.string.msg_channels)
            DrawableHelper.compoundLeftAndRightDrawable(
                text_sort_by,
                hashtagDrawable,
                arrowDownDrawable
            )
        } else {
            text_sort_by.text = getString(R.string.msg_users)
            DrawableHelper.compoundLeftAndRightDrawable(
                text_sort_by,
                userDrawable,
                arrowDownDrawable
            )
        }
    }
}