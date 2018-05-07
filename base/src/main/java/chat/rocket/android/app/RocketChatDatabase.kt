package chat.rocket.android.app

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

import chat.rocket.android.server.infraestructure.ServerDao
import chat.rocket.android.server.infraestructure.ServerEntity

@Database(entities = arrayOf(ServerEntity::class), version = 1, exportSchema = false)
abstract class RocketChatDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
}
