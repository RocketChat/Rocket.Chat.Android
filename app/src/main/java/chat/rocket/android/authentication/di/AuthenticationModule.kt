package chat.rocket.android.authentication.di

import android.content.Context
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.dagger.scope.PerActivity
import dagger.Module
import dagger.Provides

@Module
class AuthenticationModule {

    @Provides
    @PerActivity
    fun provideAuthenticationNavigator(activity: AuthenticationActivity, context: Context) = AuthenticationNavigator(activity, context)
}