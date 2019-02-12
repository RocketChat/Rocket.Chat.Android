package chat.rocket.android.contacts.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.contacts.presentation.ContactsView
import chat.rocket.android.contacts.ui.ContactsFragment
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.UserDao
import chat.rocket.android.server.domain.GetCurrentUserInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.infraestructure.ConnectionManager
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.core.RocketChatClient
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ContactsFragmentModule {

    @Provides
    @PerFragment
    fun contactsView(frag: ContactsFragment): ContactsView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: ContactsFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideRocketChatClient(
            factory: RocketChatClientFactory,
            @Named("currentServer") currentServer: String
    ): RocketChatClient {
        return factory.create(currentServer)
    }

    @Provides
    @PerFragment
    fun provideConnectionManager(
            factory: ConnectionManagerFactory,
            @Named("currentServer") currentServer: String
    ): ConnectionManager {
        return factory.create(currentServer)
    }

    @Provides
    @PerFragment
    fun providePublicSettings(
            repository: SettingsRepository,
            @Named("currentServer") currentServer: String
    ): PublicSettings {
        return repository.get(currentServer)
    }

    @Provides
    @PerFragment
    fun provideGetCurrentUserInteractor(
            tokenRepository: TokenRepository,
            @Named("currentServer") serverUrl: String,
            userDao: UserDao
    ): GetCurrentUserInteractor {
        return GetCurrentUserInteractor(tokenRepository, serverUrl, userDao)
    }
}