package chat.rocket.android.dagger.module

import chat.rocket.android.authentication.di.AuthenticationModule
import chat.rocket.android.authentication.login.di.LoginFragmentProvider
import chat.rocket.android.authentication.server.di.ServerFragmentProvider
import chat.rocket.android.authentication.signup.di.SignupFragmentProvider
import chat.rocket.android.authentication.twofactor.di.TwoFAFragmentProvider
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.chatroom.di.ChatRoomFragmentProvider
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatrooms.di.ChatRoomsFragmentProvider
import chat.rocket.android.chatrooms.di.ChatRoomsModule
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.profile.di.ProfileFragmentProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(modules = [AuthenticationModule::class,
        ServerFragmentProvider::class,
        LoginFragmentProvider::class,
        SignupFragmentProvider::class,
        TwoFAFragmentProvider::class
    ])
    abstract fun bindAuthenticationActivity(): AuthenticationActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [ChatRoomsModule::class, ChatRoomsFragmentProvider::class, ProfileFragmentProvider::class])
    abstract fun bindMainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [ChatRoomFragmentProvider::class])
    abstract fun bindChatRoomActivity(): ChatRoomActivity
}