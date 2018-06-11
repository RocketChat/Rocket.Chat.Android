package chat.rocket.android.chatinformation.di

import chat.rocket.android.chatinformation.ui.MessageInfoFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MessageInfoFragmentProvider {

    @ContributesAndroidInjector(modules = [MessageInfoFragmentModule::class])
    abstract fun provideMessageInfoFragment(): MessageInfoFragment
}