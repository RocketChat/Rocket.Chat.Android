package chat.rocket.android.members.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.members.viewmodel.MemberViewModel
import chat.rocket.android.members.viewmodel.MemberViewModelMapper
import chat.rocket.android.server.domain.ChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.getMembers
import timber.log.Timber
import javax.inject.Inject

class MembersPresenter @Inject constructor(
    private val view: MembersView,
    private val navigator: MembersNavigator,
    private val strategy: CancelStrategy,
    private val roomsInteractor: ChatRoomsInteractor,
    private val mapper: MemberViewModelMapper,
    val serverInteractor: GetCurrentServerInteractor,
    val factory: RocketChatClientFactory
) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)
    private var offset: Long = 0

    fun loadChatRoomsMembers(roomId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                roomsInteractor.getById(serverUrl, roomId)?.let {
                    val members = client.getMembers(it.id, it.type, offset, 60)
                    val memberViewModels = mapper.mapToViewModelList(members.result)
                    view.showMembers(memberViewModels, members.total)
                    offset += 1 * 60L
                }.ifNull {
                    Timber.e("Couldn't find a room with id: $roomId at current server")
                }
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun toMemberDetails(memberViewModel: MemberViewModel) {
        val avatarUri = memberViewModel.avatarUri.toString()
        val realName = memberViewModel.realName.toString()
        val username = "@${memberViewModel.username}"
        val email = memberViewModel.email ?: ""
        val utcOffset = memberViewModel.utcOffset.toString()

        navigator.toMemberDetails(avatarUri, realName, username, email, utcOffset)
    }
}