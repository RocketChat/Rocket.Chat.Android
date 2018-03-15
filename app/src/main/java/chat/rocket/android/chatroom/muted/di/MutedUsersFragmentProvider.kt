package chat.rocket.android.chatroom.muted.di

import chat.rocket.android.chatroom.muted.ui.MutedUsersFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MutedUsersFragmentProvider {
    @ContributesAndroidInjector(modules = [MutedUsersFragmentModule::class])
    abstract fun providesMutedUsersFragment(): MutedUsersFragment
}