package chat.rocket.android.inviteusers.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.inviteusers.ui.InviteUsersFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class InviteUsersFragmentProvider {

    @ContributesAndroidInjector(modules = [InviteUsersFragmentModule::class])
    @PerFragment
    abstract fun provideInviteUsersFragment(): InviteUsersFragment
}