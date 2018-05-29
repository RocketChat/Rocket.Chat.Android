package chat.rocket.android.chatrooms.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import chat.rocket.android.chatrooms.adapter.ItemHolder
import chat.rocket.android.chatrooms.adapter.RoomMapper
import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.chatrooms.infrastructure.ChatRoomsRepository
import chat.rocket.android.server.infraestructure.ConnectionManager
import chat.rocket.core.internal.realtime.socket.model.State
import kotlinx.coroutines.experimental.launch
import me.henrytao.livedataktx.distinct
import me.henrytao.livedataktx.map
import me.henrytao.livedataktx.nonNull
import timber.log.Timber

class ChatRoomsViewModel(
    private val connectionManager: ConnectionManager,
    private val interactor: FetchChatRoomsInteractor,
    private val repository: ChatRoomsRepository,
    private val mapper: RoomMapper
) : ViewModel() {
    private val ordering: MutableLiveData<ChatRoomsRepository.Order> = MutableLiveData()

    init {
        ordering.value = ChatRoomsRepository.Order.ACTIVITY
    }

    fun getChatRooms(): LiveData<List<ItemHolder<*>>> {
        // TODO - add a loading status...
        launch { interactor.refreshChatRooms() }

        return Transformations.switchMap(ordering) { order ->
            Timber.d("Querying rooms for order: $order")
            val grouped = order == ChatRoomsRepository.Order.GROUPED_ACTIVITY
                    || order == ChatRoomsRepository.Order.GROUPED_NAME
            repository.getChatRooms(order).nonNull()
                    .distinct()
                    .map { rooms ->
                        Timber.d("Mapping rooms to items: $rooms")
                        mapper.map(rooms, grouped)
                    }
        }
    }

    fun getStatus(): MutableLiveData<State> {
        return connectionManager.statusLiveData.nonNull().distinct()
    }

    fun setOrdering(order: ChatRoomsRepository.Order) {
        ordering.value = order
    }
}