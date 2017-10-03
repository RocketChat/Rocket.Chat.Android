package chat.rocket.android.dagger.module

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import chat.rocket.android.app.RocketChatDatabase
import chat.rocket.android.server.infraestructure.ServerDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule {

    @Provides
    @Singleton
    fun provideRocketChatDatabase(context: Application): RocketChatDatabase {
        return Room.databaseBuilder(context, RocketChatDatabase::class.java, "rocketchat-db").build()
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideServerDao(database: RocketChatDatabase): ServerDao {
        return database.serverDao()
    }
}
