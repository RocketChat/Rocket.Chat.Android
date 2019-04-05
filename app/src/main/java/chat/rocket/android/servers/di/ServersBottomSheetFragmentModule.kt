package chat.rocket.android.servers.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.servers.presentation.ServersView
import chat.rocket.android.servers.ui.ServersBottomSheetFragment
import dagger.Module
import dagger.Provides

@Module
class ServersBottomSheetFragmentModule {

    @Provides
    @PerFragment
    fun serversView(frag: ServersBottomSheetFragment): ServersView = frag
}