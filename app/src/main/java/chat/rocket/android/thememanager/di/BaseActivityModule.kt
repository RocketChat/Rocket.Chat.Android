package chat.rocket.android.thememanager.di

import chat.rocket.android.thememanager.BaseActivity
import dagger.Module
import dagger.Provides

@Module
class BaseActivityModule {

    @Provides
    fun provideBaseActivity() = BaseActivity()
}