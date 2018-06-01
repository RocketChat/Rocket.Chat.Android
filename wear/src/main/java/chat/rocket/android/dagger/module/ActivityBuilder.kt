package chat.rocket.android.dagger.module

import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.main.di.MainModule
import chat.rocket.android.main.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun bindMainActivity(): MainActivity
}