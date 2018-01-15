package chat.rocket.android.chatroom.di

import chat.rocket.android.chatroom.ui.ChatRoomFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChatRoomFragmentProvider {

    @ContributesAndroidInjector(modules = [ChatRoomFragmentModule::class])
    abstract fun provideChatRoomFragment(): ChatRoomFragment
}