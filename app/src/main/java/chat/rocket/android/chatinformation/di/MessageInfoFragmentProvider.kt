package chat.rocket.android.chatinformation.di

import chat.rocket.android.chatinformation.ui.MessageInfoFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MessageInfoFragmentProvider {

    @ContributesAndroidInjector(modules = [MessageInfoFragmentModule::class])
    @PerFragment
    abstract fun provideMessageInfoFragment(): MessageInfoFragment
}
