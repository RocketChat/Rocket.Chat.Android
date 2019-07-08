package chat.rocket.android.inviteusers.ui

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
import chat.rocket.android.inviteusers.presentation.InviteUsersPresenter
import chat.rocket.android.inviteusers.presentation.InviteUsersView
import chat.rocket.android.members.adapter.MembersAdapter
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import com.google.android.material.chip.Chip
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_invite_users.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

fun newInstance(chatRoomId: String): Fragment = InviteUsersFragment().apply {
    arguments = Bundle(1).apply {
        putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
    }
}

internal const val TAG_INVITE_USERS_FRAGMENT = "InviteUsersFragment"
private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"

class InviteUsersFragment : Fragment(), InviteUsersView {
    @Inject lateinit var presenter: InviteUsersPresenter
    @Inject lateinit var analyticsManager: AnalyticsManager
    private val compositeDisposable = CompositeDisposable()
    private val adapter: MembersAdapter = MembersAdapter { processSelectedMember(it) }
    private lateinit var chatRoomId: String
    private var memberList = arrayListOf<MemberUiModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            chatRoomId = getString(BUNDLE_CHAT_ROOM_ID, "")
        }
            ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_invite_users)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolBar()
        setupListeners()
        setupRecyclerView()
        subscribeEditTexts()

        analyticsManager.logScreenView(ScreenViewEvent.InviteUsers)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeEditTexts()
    }

    override fun showLoading() {
        view_loading?.isVisible = true
    }

    override fun hideLoading() {
        view_loading?.isVisible = false
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() {
        showMessage(getString(R.string.msg_generic_error))
    }

    override fun showUserSuggestion(dataSet: List<MemberUiModel>) {
        adapter.clearData()
        adapter.prependData(dataSet)
        text_member_not_found?.isVisible = false
        recycler_view?.isVisible = true
    }

    override fun showNoUserSuggestion() {
        recycler_view?.isVisible = false
        text_member_not_found?.isVisible = true
    }

    override fun showSuggestionViewInProgress() {
        recycler_view?.isVisible = false
        text_member_not_found?.isVisible = false
        showLoading()
    }

    override fun hideSuggestionViewInProgress() {
        hideLoading()
    }

    override fun usersInvitedSuccessfully() {
        memberList.clear()
        activity?.onBackPressed()
        showMessage(getString(R.string.mgs_users_invited_successfully))
    }

    override fun enableUserInput() {
        text_invite_users.isEnabled = true
    }

    override fun disableUserInput() {
        text_invite_users.isEnabled = false
    }

    private fun setupToolBar() {
        (activity as ChatRoomActivity).setupToolbarTitle((getString(R.string.msg_invite_users)))
    }

    private fun setupRecyclerView() {
        ui {
            recycler_view.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            recycler_view.addItemDecoration(
                DividerItemDecoration(it, DividerItemDecoration.HORIZONTAL)
            )
            recycler_view.adapter = adapter
        }
    }

    private fun setupListeners() {
        button_invite_user.setOnClickListener {
            if (memberList.isNotEmpty()) {
                presenter.inviteUsers(chatRoomId, memberList)
            } else {
                showMessage(R.string.mgs_choose_at_least_one_user)
            }
        }
    }

    private fun subscribeEditTexts() {
        val inviteMembersDisposable = text_invite_users.asObservable()
            .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .filter { t -> t.isNotBlank() }
            .subscribe {
                if (it.length >= 3) {
                    presenter.searchUser(it.toString())
                }
            }
        compositeDisposable.addAll(inviteMembersDisposable)
    }

    private fun unsubscribeEditTexts() = compositeDisposable.dispose()

    private fun processSelectedMember(member: MemberUiModel) {
        if (memberList.any { it.username == member.username }) {
            showMessage(getString(R.string.msg_member_already_added))
        } else {
            text_invite_users.setText("")
            addMember(member)
            addChip(member)
            chip_group_member.isVisible = true
            processBackgroundOfInviteUsersButton()
        }
    }

    private fun addMember(member: MemberUiModel) {
        memberList.add(member)
    }

    private fun removeMember(username: String) {
        memberList.remove(memberList.find { it.username == username })
    }

    private fun addChip(member: MemberUiModel) {
        val chip = Chip(context)
        chip.text = member.username
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.icon_grey)
        setupChipOnCloseIconClickListener(chip)
        chip_group_member.addView(chip)
    }

    private fun setupChipOnCloseIconClickListener(chip: Chip) {
        chip.setOnCloseIconClickListener {
            removeChip(it)
            removeMember((it as Chip).text.toString())
            // whenever we remove a chip we should process the chip group visibility.
            processChipGroupVisibility()
            processBackgroundOfInviteUsersButton()
        }
    }

    private fun removeChip(chip: View) {
        chip_group_member.removeView(chip)
    }

    private fun processChipGroupVisibility() {
        chip_group_member.isVisible = memberList.isNotEmpty()
    }

    private fun processBackgroundOfInviteUsersButton() {
        if (memberList.isEmpty()) {
            text_invite_users.alpha = 0.4F
        } else {
            text_invite_users.alpha = 1F
        }
    }
}