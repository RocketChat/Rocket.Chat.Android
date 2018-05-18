package chat.rocket.android.chatroom.di

import chat.rocket.android.favoritemessages.ui.FavoriteMessagesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FavoriteMessagesFragmentProvider {

    @ContributesAndroidInjector(modules = [FavoriteMessagesFragmentModule::class])
    abstract fun provideFavoriteMessageFragment(): FavoriteMessagesFragment
}