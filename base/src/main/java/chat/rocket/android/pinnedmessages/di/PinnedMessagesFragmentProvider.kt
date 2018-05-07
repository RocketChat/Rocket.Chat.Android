package chat.rocket.android.chatroom.di

import chat.rocket.android.pinnedmessages.ui.PinnedMessagesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PinnedMessagesFragmentProvider {

    @ContributesAndroidInjector(modules = [PinnedMessagesFragmentModule::class])
    abstract fun providePinnedMessageFragment(): PinnedMessagesFragment
}