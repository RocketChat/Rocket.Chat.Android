package chat.rocket.android.createChannel.addMembers.di

import chat.rocket.android.createChannel.addMembers.ui.AddMembersActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AddMembersProvider {
    @ContributesAndroidInjector(modules = [AddMembersModule::class])
    abstract fun provideNewChannelActivity(): AddMembersActivity
}