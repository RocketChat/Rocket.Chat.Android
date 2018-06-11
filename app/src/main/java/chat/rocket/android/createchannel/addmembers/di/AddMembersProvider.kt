package chat.rocket.android.createchannel.addmembers.di

import chat.rocket.android.createchannel.addmembers.ui.AddMembersActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AddMembersProvider {
    @ContributesAndroidInjector(modules = [AddMembersModule::class])
    abstract fun provideAddMembersActivity(): AddMembersActivity
}