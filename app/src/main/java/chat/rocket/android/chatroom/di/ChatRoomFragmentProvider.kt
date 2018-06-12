package chat.rocket.android.chatroom.di

import chat.rocket.android.chatroom.ui.ChatRoomFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChatRoomFragmentProvider {

    @ContributesAndroidInjector(modules = [ChatRoomFragmentModule::class])
    @PerFragment
    abstract fun provideChatRoomFragment(): ChatRoomFragment
}