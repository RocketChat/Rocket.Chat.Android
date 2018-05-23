package chat.rocket.android.createChannel.addMembers.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.members.viewmodel.MemberViewModelMapper
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.queryUsers
import javax.inject.Inject

class AddMembersPresenter @Inject constructor(
    private val view: AddMembersView,
    private val strategy: CancelStrategy,
    private val serverInteractor: GetCurrentServerInteractor,
    private val mapper: MemberViewModelMapper,
    factory: RocketChatClientFactory
) {
    val serverUrl: String? = serverInteractor.get()
    private val client = if (serverUrl != null) factory.create(serverUrl) else null

    fun queryUsersFromRegex(queryParam: String) {
        if (client != null) {
            launchUI(strategy) {
                try {
                    val allMembers = retryIO("queryUsers($queryParam)") {
                        client.queryUsers(queryParam)
                    }
                    val memberViewModelMapper = mapper.mapToViewModelList(allMembers.result)
                } catch (ex: RocketChatException) {

                }
            }
        }
    }
}