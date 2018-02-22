package chat.rocket.android.main.di

import android.content.Context
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.main.ui.MainActivity
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    @PerActivity
    fun provideMainNavigator(activity: MainActivity, context: Context) = MainNavigator(activity, context)
}