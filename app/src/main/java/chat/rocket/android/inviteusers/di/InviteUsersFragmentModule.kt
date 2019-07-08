package chat.rocket.android.inviteusers.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.inviteusers.presentation.InviteUsersView
import chat.rocket.android.inviteusers.ui.InviteUsersFragment
import dagger.Module
import dagger.Provides

@Module
class InviteUsersFragmentModule {

    @Provides
    @PerFragment
    fun inviteUsersView(frag: InviteUsersFragment): InviteUsersView = frag
}