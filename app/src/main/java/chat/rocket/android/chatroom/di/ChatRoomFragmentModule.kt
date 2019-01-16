package chat.rocket.android.chatroom.di

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.ui.ChatRoomFragment
import chat.rocket.android.chatrooms.adapter.RoomUiModelMapper
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.ChatRoomDao
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.UserDao
import chat.rocket.android.server.domain.GetCurrentUserInteractor
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.TokenRepository
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ChatRoomFragmentModule {

    @Provides
    @PerFragment
    fun chatRoomView(frag: ChatRoomFragment): ChatRoomView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: ChatRoomFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideChatRoomDao(manager: DatabaseManager): ChatRoomDao = manager.chatRoomDao()

    @Provides
    @PerFragment
    fun provideUserDao(manager: DatabaseManager): UserDao = manager.userDao()

    @Provides
    @PerFragment
    fun provideGetCurrentUserInteractor(
        tokenRepository: TokenRepository,
        @Named("currentServer") serverUrl: String,
        userDao: UserDao
    ): GetCurrentUserInteractor {
        return GetCurrentUserInteractor(tokenRepository, serverUrl, userDao)
    }

    @Provides
    @PerFragment
    fun provideRoomMapper(
        context: Application,
        repository: SettingsRepository,
        userInteractor: GetCurrentUserInteractor,
        @Named("currentServer") serverUrl: String,
        permissionsInteractor: PermissionsInteractor
    ): RoomUiModelMapper {
        return RoomUiModelMapper(
            context,
            repository.get(serverUrl),
            userInteractor,
            serverUrl,
            permissionsInteractor
        )
    }
}
