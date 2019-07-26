package chat.rocket.android.chatrooms.di

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.chatrooms.adapter.RoomUiModelMapper
import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.ChatRoomDao
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.UserDao
import chat.rocket.android.server.domain.GetCurrentUserInteractor
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.infrastructure.ConnectionManager
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.core.RocketChatClient
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ChatRoomsFragmentModule {

    @Provides
    @PerFragment
    fun chatRoomsView(frag: ChatRoomsFragment): ChatRoomsView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: ChatRoomsFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideRocketChatClient(
        factory: RocketChatClientFactory,
        @Named("currentServer") currentServer: String?
    ): RocketChatClient {
        return currentServer?.let { factory.get(it) }!!
    }

    @Provides
    @PerFragment
    fun provideChatRoomDao(manager: DatabaseManager): ChatRoomDao = manager.chatRoomDao()

    @Provides
    @PerFragment
    fun provideUserDao(manager: DatabaseManager): UserDao = manager.userDao()

    @Provides
    @PerFragment
    fun provideConnectionManager(
        factory: ConnectionManagerFactory,
        @Named("currentServer") currentServer: String?
    ): ConnectionManager {
        return currentServer?.let { factory.create(it) }!!
    }

    @Provides
    @PerFragment
    fun provideFetchChatRoomsInteractor(
        client: RocketChatClient,
        dbManager: DatabaseManager
    ): FetchChatRoomsInteractor {
        return FetchChatRoomsInteractor(client, dbManager)
    }

    @Provides
    @PerFragment
    fun providePublicSettings(
        repository: SettingsRepository,
        @Named("currentServer") currentServer: String?
    ): PublicSettings {
        return currentServer?.let { repository.get(it) }!!
    }

    @Provides
    @PerFragment
    fun provideRoomMapper(
        context: Application,
        settingsRepository: SettingsRepository,
        userInteractor: GetCurrentUserInteractor,
        tokenRepository: TokenRepository,
        @Named("currentServer") currentServer: String?,
        permissionsInteractor: PermissionsInteractor
    ): RoomUiModelMapper {
        return currentServer?.let {
            RoomUiModelMapper(
                context,
                settingsRepository.get(it),
                userInteractor,
                tokenRepository,
                it,
                permissionsInteractor
            )
        }!!
    }

    @Provides
    @PerFragment
    fun provideGetCurrentUserInteractor(
        tokenRepository: TokenRepository,
        @Named("currentServer") currentServer: String?,
        userDao: UserDao
    ): GetCurrentUserInteractor {
        return currentServer?.let { GetCurrentUserInteractor(tokenRepository, it, userDao) }!!
    }
}