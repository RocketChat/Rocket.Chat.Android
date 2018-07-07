package chat.rocket.android.dagger.module

import chat.rocket.android.authentication.di.AuthenticationModule
import chat.rocket.android.authentication.login.di.LoginFragmentProvider
import chat.rocket.android.authentication.registerusername.di.RegisterUsernameFragmentProvider
import chat.rocket.android.authentication.resetpassword.di.ResetPasswordFragmentProvider
import chat.rocket.android.authentication.server.di.ServerFragmentProvider
import chat.rocket.android.authentication.signup.di.SignupFragmentProvider
import chat.rocket.android.authentication.twofactor.di.TwoFAFragmentProvider
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.chatroom.di.ChatRoomFragmentProvider
import chat.rocket.android.chatroom.di.ChatRoomModule
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatrooms.di.ChatRoomsFragmentProvider
import chat.rocket.android.createchannel.di.CreateChannelProvider
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.favoritemessages.di.FavoriteMessagesFragmentProvider
import chat.rocket.android.files.di.FilesFragmentProvider
import chat.rocket.android.main.di.MainModule
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.members.di.MembersFragmentProvider
import chat.rocket.android.mentions.di.MentionsFragmentProvider
import chat.rocket.android.pinnedmessages.di.PinnedMessagesFragmentProvider
import chat.rocket.android.profile.di.ProfileFragmentProvider
import chat.rocket.android.server.di.ChangeServerModule
import chat.rocket.android.server.ui.ChangeServerActivity
import chat.rocket.android.settings.di.SettingsFragmentProvider
import chat.rocket.android.settings.password.di.PasswordFragmentProvider
import chat.rocket.android.settings.password.ui.PasswordActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(
        modules = [AuthenticationModule::class,
            ServerFragmentProvider::class,
            LoginFragmentProvider::class,
            RegisterUsernameFragmentProvider::class,
            ResetPasswordFragmentProvider::class,
            SignupFragmentProvider::class,
            TwoFAFragmentProvider::class
        ]
    )
    abstract fun bindAuthenticationActivity(): AuthenticationActivity

    @PerActivity
    @ContributesAndroidInjector(
        modules = [MainModule::class,
            ChatRoomsFragmentProvider::class,
            CreateChannelProvider::class,
            ProfileFragmentProvider::class,
            SettingsFragmentProvider::class
        ]
    )
    abstract fun bindMainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector(
        modules = [
            ChatRoomModule::class,
            ChatRoomFragmentProvider::class,
            MembersFragmentProvider::class,
            MentionsFragmentProvider::class,
            PinnedMessagesFragmentProvider::class,
            FavoriteMessagesFragmentProvider::class,
            FilesFragmentProvider::class
        ]
    )
    abstract fun bindChatRoomActivity(): ChatRoomActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [PasswordFragmentProvider::class])
    abstract fun bindPasswordActivity(): PasswordActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [ChangeServerModule::class])
    abstract fun bindChangeServerActivity(): ChangeServerActivity
}