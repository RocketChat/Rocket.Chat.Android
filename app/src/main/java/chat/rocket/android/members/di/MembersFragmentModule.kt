package chat.rocket.android.members.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.members.presentation.MembersView
import chat.rocket.android.members.ui.MembersFragment
import dagger.Module
import dagger.Provides

@Module
class MembersFragmentModule {

    @Provides
    @PerFragment
    fun membersView(frag: MembersFragment): MembersView {
        return frag
    }
}