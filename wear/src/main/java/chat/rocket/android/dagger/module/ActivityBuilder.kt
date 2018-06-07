package chat.rocket.android.dagger.module

import chat.rocket.android.chatrooms.di.ChatRoomsFragmentProvider
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.main.di.MainModule
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.starter.di.StarterActivityModule
import chat.rocket.android.starter.ui.StarterActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(modules = [StarterActivityModule::class])
    abstract fun bindStarterActivity(): StarterActivity

    @PerActivity
    @ContributesAndroidInjector(
        modules = [MainModule::class,
            ChatRoomsFragmentProvider::class]
    )
    abstract fun bindMainActivity(): MainActivity
}