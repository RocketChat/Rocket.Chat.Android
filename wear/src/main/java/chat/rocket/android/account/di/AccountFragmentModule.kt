package chat.rocket.android.account.di

import chat.rocket.android.account.presentation.AccountView
import chat.rocket.android.account.ui.AccountFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
class AccountFragmentModule {

    @Provides
    @PerFragment
    fun accountView(frag: AccountFragment): AccountView {
        return frag
    }
}