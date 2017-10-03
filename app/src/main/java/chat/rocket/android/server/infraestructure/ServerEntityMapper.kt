package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.model.Server
import chat.rocket.android.util.DataToDomain

class ServerEntityMapper : DataToDomain<ServerEntity, Server> {
    override fun translate(serverEntity: ServerEntity): Server {
        return Server(serverEntity.id, serverEntity.name, serverEntity.host, serverEntity.avatar)
    }
}
