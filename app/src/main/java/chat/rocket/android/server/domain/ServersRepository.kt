package chat.rocket.android.server.domain

import chat.rocket.android.server.domain.model.Server
import chat.rocket.android.server.infraestructure.ServerEntity
import io.reactivex.Completable
import io.reactivex.Single

interface ServersRepository {
    val servers: Single<List<Server>>

    fun saveServer(server: Server): Completable

    fun updateServer(server: Server): Completable
}