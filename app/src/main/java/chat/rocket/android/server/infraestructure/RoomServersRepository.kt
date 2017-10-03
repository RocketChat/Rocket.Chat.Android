package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.ServersRepository
import chat.rocket.android.server.domain.model.Server
import io.reactivex.Completable
import io.reactivex.Single

class RoomServersRepository : ServersRepository {

    override val servers: Single<List<Server>>
        get() = TODO("not implemented")

    override fun saveServer(server: Server): Completable {
        TODO("not implemented")
    }

    override fun updateServer(server: Server): Completable {
        TODO("not implemented")
    }
}
