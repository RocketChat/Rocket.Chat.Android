package chat.rocket.android.sortingandgrouping.ui

import DrawableHelper
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import chat.rocket.android.R
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.chatrooms.ui.TAG_CHAT_ROOMS_FRAGMENT
import chat.rocket.android.sortingandgrouping.presentation.SortingAndGroupingPresenter
import chat.rocket.android.sortingandgrouping.presentation.SortingAndGroupingView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.bottom_sheet_fragment_sort_by.*
import javax.inject.Inject

const val TAG = "SortingAndGroupingBottomSheetFragment"

class SortingAndGroupingBottomSheetFragment : BottomSheetDialogFragment(), SortingAndGroupingView {
    @Inject
    lateinit var presenter: SortingAndGroupingPresenter
    private var isSortByName = false
    private var isUnreadOnTop = false
    private var isGroupByType = false
    private var isGroupByFavorites = false
    private val chatRoomFragment by lazy {
        activity?.supportFragmentManager?.findFragmentByTag(TAG_CHAT_ROOMS_FRAGMENT) as ChatRoomsFragment
    }
    private val filterDrawable by lazy { R.drawable.ic_filter_20dp }
    private val activityDrawable by lazy { R.drawable.ic_activity_20dp }
    private val unreadOnTopDrawable by lazy { R.drawable.ic_unread_20dp }
    private val groupByTypeDrawable by lazy { R.drawable.ic_group_by_type_20dp }
    private val groupByFavoritesDrawable by lazy { R.drawable.ic_favorites_20dp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.bottom_sheet_fragment_sort_by, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.getSortingAndGroupingPreferences()
        setupListeners()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        presenter.saveSortingAndGroupingPreferences(
            isSortByName,
            isUnreadOnTop,
            isGroupByType,
            isGroupByFavorites
        )
    }

    override fun showSortingAndGroupingPreferences(
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    ) {
        this.isSortByName = isSortByName
        this.isUnreadOnTop = isUnreadOnTop
        this.isGroupByType = isGroupByType
        this.isGroupByFavorites = isGroupByFavorites

        if (isSortByName) {
            changeSortByTitle(getString(R.string.msg_sort_by_name))
            checkSelection(text_name, filterDrawable)
        } else {
            changeSortByTitle(getString(R.string.msg_sort_by_activity))
            checkSelection(text_activity, activityDrawable)
        }

        if (isUnreadOnTop) checkSelection(text_unread_on_top, unreadOnTopDrawable)
        if (isGroupByType) checkSelection(text_group_by_type, groupByTypeDrawable)
        if (isGroupByFavorites) checkSelection(text_group_by_favorites, groupByFavoritesDrawable)
    }

    private fun setupListeners() {
        text_name.setOnClickListener {
            changeSortByTitle(getString(R.string.msg_sort_by_name))
            checkSelection(text_name, filterDrawable)
            uncheckSelection(text_activity, activityDrawable)
            isSortByName = true
            sortChatRoomsList()
        }

        text_activity.setOnClickListener {
            changeSortByTitle(getString(R.string.msg_sort_by_activity))
            checkSelection(text_activity, activityDrawable)
            uncheckSelection(text_name, filterDrawable)
            isSortByName = false
            sortChatRoomsList()
        }

        text_unread_on_top.setOnClickListener {
            isUnreadOnTop = if (isUnreadOnTop) {
                uncheckSelection(text_unread_on_top, unreadOnTopDrawable)
                false
            } else {
                checkSelection(text_unread_on_top, unreadOnTopDrawable)
                true
            }
            sortChatRoomsList()
        }

        text_group_by_type.setOnClickListener {
            isGroupByType = if (isGroupByType) {
                uncheckSelection(text_group_by_type, groupByTypeDrawable)
                false
            } else {
                checkSelection(text_group_by_type, groupByTypeDrawable)
                true
            }
            sortChatRoomsList()
        }

        text_group_by_favorites.setOnClickListener {
            isGroupByFavorites = if (isGroupByFavorites) {
                uncheckSelection(text_group_by_favorites, groupByFavoritesDrawable)
                false
            } else {
                checkSelection(text_group_by_favorites, groupByFavoritesDrawable)
                true
            }
            sortChatRoomsList()
        }
    }

    private fun changeSortByTitle(text: String) {
        text_sort_by.text = getString(R.string.msg_sort_by_placeholder, text.toLowerCase())
    }

    private fun checkSelection(textView: TextView, @DrawableRes startDrawable: Int) {
        context?.let {
            DrawableHelper.compoundStartAndEndDrawable(
                textView,
                DrawableHelper.getDrawableFromId(startDrawable, it),
                DrawableHelper.getDrawableFromId(R.drawable.ic_check, it)
            )
        }
    }

    private fun uncheckSelection(textView: TextView, @DrawableRes startDrawable: Int) {
        context?.let {
            DrawableHelper.compoundStartDrawable(
                textView,
                DrawableHelper.getDrawableFromId(startDrawable, it)
            )
        }
    }

    private fun sortChatRoomsList() {
        chatRoomFragment.sortChatRoomsList(
            isSortByName,
            isUnreadOnTop,
            isGroupByType,
            isGroupByFavorites
        )
    }
}