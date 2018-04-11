package chat.rocket.android.dagger.module

import chat.rocket.android.authentication.di.AuthenticationModule
import chat.rocket.android.authentication.login.di.LoginFragmentProvider
import chat.rocket.android.authentication.registerusername.di.RegisterUsernameFragmentProvider
import chat.rocket.android.authentication.server.di.ServerFragmentProvider
import chat.rocket.android.authentication.signup.di.SignupFragmentProvider
import chat.rocket.android.authentication.twofactor.di.TwoFAFragmentProvider
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.chatroom.di.ChatRoomFragmentProvider
import chat.rocket.android.chatroom.di.PinnedMessagesFragmentProvider
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.ui.PinnedMessagesActivity
import chat.rocket.android.chatrooms.di.ChatRoomsFragmentProvider
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.main.di.MainModule
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.members.di.MembersFragmentProvider
import chat.rocket.android.profile.di.ProfileFragmentProvider
import chat.rocket.android.server.di.ChangeServerModule
import chat.rocket.android.server.ui.ChangeServerActivity
import chat.rocket.android.settings.password.di.PasswordFragmentProvider
import chat.rocket.android.settings.password.ui.PasswordActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(modules = [AuthenticationModule::class,
        ServerFragmentProvider::class,
        LoginFragmentProvider::class,
        RegisterUsernameFragmentProvider::class,
        SignupFragmentProvider::class,
        TwoFAFragmentProvider::class
    ])
    abstract fun bindAuthenticationActivity(): AuthenticationActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [MainModule::class,
        ChatRoomsFragmentProvider::class,
        ProfileFragmentProvider::class
    ])
    abstract fun bindMainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [ChatRoomFragmentProvider::class, MembersFragmentProvider::class])
    abstract fun bindChatRoomActivity(): ChatRoomActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [PinnedMessagesFragmentProvider::class])
    abstract fun bindPinnedMessagesActivity(): PinnedMessagesActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [PasswordFragmentProvider::class])
    abstract fun bindPasswordActivity(): PasswordActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [ChangeServerModule::class])
    abstract fun bindChangeServerActivity(): ChangeServerActivity
}