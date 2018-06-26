package chat.rocket.android.pinnedmessages.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.pinnedmessages.ui.PinnedMessagesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PinnedMessagesFragmentProvider {

    @ContributesAndroidInjector(modules = [PinnedMessagesFragmentModule::class])
    @PerFragment
    abstract fun providePinnedMessageFragment(): PinnedMessagesFragment
}