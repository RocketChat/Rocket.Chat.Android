package chat.rocket.android.authentication.di

import android.content.Context
import chat.rocket.android.authentication.infraestructure.SharedPreferencesMultiServerTokenRepository
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.MultiServerTokenRepository
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides

@Module
class AuthenticationModule {

    @Provides
    @PerActivity
    fun provideAuthenticationNavigator(activity: AuthenticationActivity, context: Context) = AuthenticationNavigator(activity, context)

    @Provides
    @PerActivity
    fun provideMultiServerTokenRepository(repository: LocalRepository, moshi: Moshi): MultiServerTokenRepository {
        return SharedPreferencesMultiServerTokenRepository(repository, moshi)
    }
}