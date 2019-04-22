package chat.rocket.android.preferences.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.preferences.presentation.PreferencesView
import chat.rocket.android.preferences.ui.PreferencesFragment
import dagger.Module
import dagger.Provides

@Module
class PreferencesFragmentModule {

    @Provides
    @PerFragment
    fun preferencesView(frag: PreferencesFragment): PreferencesView {
        return frag
    }
}