package chat.rocket.android.chatrooms.di

import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChatRoomsFragmentProvider {

    @ContributesAndroidInjector(modules = [ChatRoomsFragmentModule::class])
    abstract fun provideChatRoomsFragment(): ChatRoomsFragment
}