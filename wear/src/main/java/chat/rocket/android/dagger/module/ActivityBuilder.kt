package chat.rocket.android.dagger.module

import chat.rocket.android.account.di.AccountFragmentProvider
import chat.rocket.android.chatroom.di.ChatRoomFragmentProvider
import chat.rocket.android.chatroom.di.ChatRoomModule
import chat.rocket.android.chatroom.reply.di.ReplyMessageFragmentProvider
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatrooms.di.ChatRoomsFragmentProvider
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.main.di.MainModule
import chat.rocket.android.main.settings.di.SettingsFragmentProvider
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.starter.di.StarterActivityModule
import chat.rocket.android.starter.ui.StarterActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(modules = [StarterActivityModule::class])
    abstract fun bindStarterActivity(): StarterActivity

    @PerActivity
    @ContributesAndroidInjector(
        modules = [MainModule::class,
            ChatRoomsFragmentProvider::class,
            SettingsFragmentProvider::class,
            AccountFragmentProvider::class]
    )
    abstract fun bindMainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector(
        modules = [ChatRoomModule::class,
            ChatRoomFragmentProvider::class,
            ReplyMessageFragmentProvider::class]
    )
    abstract fun bindChatRoomActivity(): ChatRoomActivity
}