package chat.rocket.android.app

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

import chat.rocket.android.server.infraestructure.ServerDao
import chat.rocket.android.server.infraestructure.ServerEntity
import chat.rocket.android.room.weblink.WebLinkDao
import chat.rocket.android.room.weblink.WebLinkEntity

@Database(entities = [(ServerEntity::class), (WebLinkEntity::class)], version = 1, exportSchema = false)
abstract class RocketChatDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun webLinkDao(): WebLinkDao
}
