package chat.rocket.android.directory.ui

import DrawableHelper
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import chat.rocket.android.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_fragment_directory_sorting.*

fun showDirectorySortingBottomSheetFragment(
    isSortByChannels: Boolean,
    isSearchForGlobalUsers: Boolean,
    supportFragmentManager: FragmentManager
) = DirectorySortingBottomSheetFragment().apply {
    arguments = Bundle(2).apply {
        putBoolean(BUNDLE_IS_SORT_BY_CHANNELS, isSortByChannels)
        putBoolean(BUNDLE_IS_SEARCH_FOR_GLOBAL_USERS, isSearchForGlobalUsers)
    }
}.show(supportFragmentManager, TAG)

internal const val TAG = "DirectorySortingBottomSheetFragment"

private const val BUNDLE_IS_SORT_BY_CHANNELS = "is_sort_by_channels"
private const val BUNDLE_IS_SEARCH_FOR_GLOBAL_USERS = "is_search_for_global_users"

class DirectorySortingBottomSheetFragment : BottomSheetDialogFragment() {
    private var isSortByChannels = true
    private var isSearchForGlobalUsers = false
    private val hashtagDrawable by lazy {
        DrawableHelper.getDrawableFromId(R.drawable.ic_hashtag_16dp, requireContext())
    }
    private val userDrawable by lazy {
        DrawableHelper.getDrawableFromId(R.drawable.ic_user_16dp, requireContext())
    }
    private val checkDrawable by lazy {
        DrawableHelper.getDrawableFromId(R.drawable.ic_check, requireContext())
    }
    private val directoryFragment by lazy {
        activity?.supportFragmentManager?.findFragmentByTag(TAG_DIRECTORY_FRAGMENT) as DirectoryFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            isSortByChannels = getBoolean(BUNDLE_IS_SORT_BY_CHANNELS)
            isSearchForGlobalUsers = getBoolean(BUNDLE_IS_SEARCH_FOR_GLOBAL_USERS)
        }
            ?: requireNotNull(arguments) { "no arguments supplied when the bottom sheet fragment was instantiated" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.bottom_sheet_fragment_directory_sorting, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupListeners()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
    }

    private fun setupView() {
        if (isSortByChannels) {
            checkSelection(text_channels, hashtagDrawable)
        } else {
            checkSelection(text_users, userDrawable)
        }

        switch_global_users.isChecked = isSearchForGlobalUsers
    }

    private fun setupListeners() {
        text_channels.setOnClickListener {
            checkSelection(text_channels, hashtagDrawable)
            uncheckSelection(text_users, userDrawable)
            isSortByChannels = true
            directoryFragment.updateSorting(isSortByChannels, isSearchForGlobalUsers)
        }

        text_users.setOnClickListener {
            checkSelection(text_users, userDrawable)
            uncheckSelection(text_channels, hashtagDrawable)
            isSortByChannels = false
            directoryFragment.updateSorting(isSortByChannels, isSearchForGlobalUsers)
        }

        switch_global_users.setOnCheckedChangeListener { _, isChecked ->
            isSearchForGlobalUsers = isChecked
            directoryFragment.updateSorting(isSortByChannels, isSearchForGlobalUsers)
        }
    }

    private fun checkSelection(textView: TextView, startDrawable: Drawable) {
        context?.let {
            DrawableHelper.compoundStartAndEndDrawable(
                textView,
                startDrawable,
                checkDrawable
            )
        }
    }

    private fun uncheckSelection(textView: TextView, startDrawable: Drawable) {
        context?.let {
            DrawableHelper.compoundStartDrawable(
                textView,
                startDrawable
            )
        }
    }
}