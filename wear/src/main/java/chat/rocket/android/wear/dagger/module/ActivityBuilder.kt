package chat.rocket.android.wear.dagger.module

import chat.rocket.android.wear.dagger.scope.PerActivity
import chat.rocket.android.wear.main.di.MainModule
import chat.rocket.android.wear.main.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun bindMainActivity(): MainActivity
}