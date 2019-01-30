package chat.rocket.android.favoritemessages.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.favoritemessages.presentation.FavoriteMessagesView
import chat.rocket.android.favoritemessages.ui.FavoriteMessagesFragment
import dagger.Module
import dagger.Provides

@Module
class FavoriteMessagesFragmentModule {

    @Provides
    @PerFragment
    fun provideFavoriteMessagesView(frag: FavoriteMessagesFragment): FavoriteMessagesView {
        return frag
    }
}