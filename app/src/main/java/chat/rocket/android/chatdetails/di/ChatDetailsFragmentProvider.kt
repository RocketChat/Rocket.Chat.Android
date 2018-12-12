package chat.rocket.android.chatdetails.di

import chat.rocket.android.chatdetails.ui.ChatDetailsFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChatDetailsFragmentProvider {

    @ContributesAndroidInjector(modules = [ChatDetailsFragmentModule::class])
    @PerFragment
    abstract fun providesChatDetailsFragment(): ChatDetailsFragment
}