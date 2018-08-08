package chat.rocket.android.wallet.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.ChatRoomDao
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.wallet.presentation.WalletView
import chat.rocket.android.wallet.ui.WalletFragment
import dagger.Module
import dagger.Provides

@Module
class WalletFragmentModule {

    @Provides
    @PerFragment
    fun walletView(frag: WalletFragment): WalletView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: WalletFragment): LifecycleOwner {
        return frag
    }


    @Provides
    @PerFragment
    fun provideChatRoomDao(manager: DatabaseManager): ChatRoomDao = manager.chatRoomDao()

}