package chat.rocket.android.createchannel.addmembers.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.members.viewmodel.MemberViewModelMapper
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.searchUser
import javax.inject.Inject

class AddMembersPresenter @Inject constructor(
    private val view: AddMembersView,
    private val strategy: CancelStrategy,
    private val mapper: MemberViewModelMapper,
    val serverInteractor: GetCurrentServerInteractor,
    val factory: RocketChatClientFactory
) {
    private val client = factory.create(serverInteractor.get()!!)
    private var offset: Long = 0

    fun searchUser(query: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                val users = client.searchUser(query, offset)
                val memberViewModelMapper = mapper.mapToViewModelList(users.result)
                view.showUsers(memberViewModelMapper, users.total)
                offset += 1 * 30L
            } catch (ex: RocketChatException) {
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }
}