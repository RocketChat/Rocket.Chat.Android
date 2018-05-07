package chat.rocket.android.app.migration

import chat.rocket.android.app.migration.model.RealmBasedServerInfo
import io.realm.annotations.RealmModule

@RealmModule(library = true, classes = arrayOf(RealmBasedServerInfo::class))
class RocketChatServerModule