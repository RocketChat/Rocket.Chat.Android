package chat.rocket.android.members.di

import chat.rocket.android.members.ui.MembersFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MembersFragmentProvider {

    @ContributesAndroidInjector(modules = [MembersFragmentModule::class])
    abstract fun provideMembersFragment(): MembersFragment
}