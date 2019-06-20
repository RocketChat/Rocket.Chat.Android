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
import chat.rocket.android.inviteusers.adapter.SelectedUsersAdapter
import chat.rocket.android.inviteusers.presentation.InviteUsersPresenter
import chat.rocket.android.inviteusers.presentation.InviteUsersView
import chat.rocket.android.members.adapter.MembersAdapter
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
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

	@Inject
	lateinit var presenter: InviteUsersPresenter
	@Inject
	lateinit var analyticsManager: AnalyticsManager
	private val compositeDisposable = CompositeDisposable()
	private var memberList = arrayListOf<MemberUiModel>()

	private val adapter: MembersAdapter = MembersAdapter {
		it.username?.run { processSelectedMember(it) }
	}

	private val selectedUsersAdapter = SelectedUsersAdapter(memberList, true) { user ->
		user.username?.let { removeMember(it) }
		// whenever we remove a user we should process the recycler view visibility.
		processRecyclerViewVisibility()
		processBackgroundOfInviteUsersButton()
	}

	private lateinit var chatRoomId: String

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
		setupViewListeners()
		setupRecyclerView()
		subscribeEditTexts()

		analyticsManager.logScreenView(ScreenViewEvent.InviteUsers)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		unsubscribeEditTexts()
	}

	override fun showLoading() {
		ui {
			view_loading.isVisible = true
		}
	}

	override fun hideLoading() {
		ui {
			view_loading.isVisible = false
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

	override fun showUserSuggestion(dataSet: List<MemberUiModel>) {
		adapter.clearData()
		adapter.prependData(dataSet)
		text_member_not_found.isVisible = false
		recycler_view.isVisible = true
		view_member_suggestion.isVisible = true
		showLoading()
	}

	override fun showNoUserSuggestion() {
		recycler_view.isVisible = false
		text_member_not_found.isVisible = true
		view_member_suggestion.isVisible = true
	}

	override fun showSuggestionViewInProgress() {
		recycler_view.isVisible = false
		text_member_not_found.isVisible = false
		view_member_suggestion.isVisible = true
		showLoading()
	}

	override fun hideSuggestionViewInProgress() {
		hideLoading()
	}

	override fun usersInvitedSuccessfully() {
		memberList.clear()
		activity?.onBackPressed()
	}

	override fun enableUserInput() {
		edit_text_invite_users.isEnabled = true
	}

	override fun disableUserInput() {
		edit_text_invite_users.isEnabled = false
	}

	private fun setupToolBar() {
		(activity as ChatRoomActivity).setupToolbarTitle((getString(R.string.title_invite_users)))
	}

	private fun setupRecyclerView() {
		ui {
			recycler_view.layoutManager =
				LinearLayoutManager(context, RecyclerView.VERTICAL, false)
			recycler_view.addItemDecoration(
				DividerItemDecoration(it, DividerItemDecoration.HORIZONTAL)
			)
			recycler_view.adapter = adapter

			selected_users_recycler_view.apply {
				setHasFixedSize(true)
				layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
				adapter = selectedUsersAdapter
			}
		}
	}

	private fun setupViewListeners() {
		invite_users_fab?.setOnClickListener {
			if (memberList.isEmpty()) {
				showToast("Select at least one user")
			} else {
				presenter.inviteUsers(chatRoomId, memberList)
			}
		}
	}

	private fun subscribeEditTexts() {

		val inviteMembersDisposable = edit_text_invite_users.asObservable()
			.debounce(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
			.filter { t -> t.isNotBlank() }
			.subscribe {
				if (it.length >= 3) {
					presenter.searchUser(it.toString())
				} else {
					view_member_suggestion.isVisible = false
				}
			}

		compositeDisposable.addAll(inviteMembersDisposable)
	}

	private fun unsubscribeEditTexts() {
		compositeDisposable.dispose()
	}

	private fun processSelectedMember(member: MemberUiModel) {
		if (memberList.any { it.username == member.username }) {
			showMessage(getString(R.string.msg_member_already_added))
		} else {
			view_member_suggestion.isVisible = false
			edit_text_invite_users.setText("")
			addMember(member)
			processRecyclerViewVisibility()
			processBackgroundOfInviteUsersButton()
		}
	}

	private fun addMember(member: MemberUiModel) {
		memberList.add(member)
		selectedUsersAdapter.notifyDataSetChanged()
	}

	private fun removeMember(username: String) {
		memberList.remove(memberList.find { it.username == username })
		selectedUsersAdapter.notifyDataSetChanged()
	}

	private fun processRecyclerViewVisibility() {
		selected_users_recycler_view.isVisible = memberList.isNotEmpty()
		selected_users_divider.isVisible = memberList.isNotEmpty()
	}

	private fun processBackgroundOfInviteUsersButton() {
		if (memberList.isEmpty()) {
			invite_users_fab.alpha = 0.4F
		} else {
			invite_users_fab.alpha = 1F
		}
	}
}