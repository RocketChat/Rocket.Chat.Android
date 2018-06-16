package chat.rocket.android.chatrooms.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import chat.rocket.android.chatrooms.adapter.ItemHolder
import chat.rocket.android.chatrooms.adapter.RoomUiModelMapper
import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.chatrooms.infrastructure.ChatRoomsRepository
import chat.rocket.android.chatrooms.infrastructure.isGrouped
import chat.rocket.android.server.infraestructure.ConnectionManager
import chat.rocket.android.util.livedata.transform
import chat.rocket.core.internal.realtime.socket.model.State
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import me.henrytao.livedataktx.distinct
import me.henrytao.livedataktx.map
import me.henrytao.livedataktx.nonNull
import timber.log.Timber

class ChatRoomsViewModel(
    private val connectionManager: ConnectionManager,
    private val interactor: FetchChatRoomsInteractor,
    private val repository: ChatRoomsRepository,
    private val mapper: RoomUiModelMapper
) : ViewModel() {
    private val ordering: MutableLiveData<ChatRoomsRepository.Order> = MutableLiveData()
    private val runContext = newSingleThreadContext("chat-rooms-view-model")

    init {
        ordering.value = ChatRoomsRepository.Order.ACTIVITY
    }

    fun getChatRooms(): LiveData<List<ItemHolder<*>>> {
        return Transformations.switchMap(ordering) { order ->
            Timber.d("Querying rooms for order: $order")
            repository.getChatRooms(order)
                    .nonNull()
                    .distinct()
                    .transform(runContext) { rooms ->
                        rooms?.let {
                            mapper.map(rooms, order.isGrouped())
                        }
                    }
        }
    }

    fun getStatus(): MutableLiveData<State> {
        return connectionManager.statusLiveData.nonNull().distinct().map { state ->
            if (state is State.Connected) {
                // TODO - add a loading status...
                fetchRooms()
            }
            state
        }
    }

    private fun fetchRooms() {
        launch {
            interactor.refreshChatRooms()
        }
    }

    fun setOrdering(order: ChatRoomsRepository.Order) {
        ordering.value = order
    }
}
