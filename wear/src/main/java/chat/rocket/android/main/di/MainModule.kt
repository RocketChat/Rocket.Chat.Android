package chat.rocket.android.main.di

import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.main.presentation.MainView
import chat.rocket.android.main.ui.MainActivity
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    @PerActivity
    fun createMainActivityView(activity: MainActivity): MainView {
        return activity
    }
}