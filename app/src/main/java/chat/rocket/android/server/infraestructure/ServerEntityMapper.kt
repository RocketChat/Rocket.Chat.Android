package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.model.Server
import chat.rocket.android.util.DataToDomain

class ServerEntityMapper : DataToDomain<ServerEntity, Server> {
    override fun translate(data: ServerEntity): Server {
        return Server(data.id, data.name, data.host, data.avatar)
    }
}
