package chat.rocket.android.settings.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.settings.presentation.SettingsView
import chat.rocket.android.settings.ui.SettingsFragment
import dagger.Module
import dagger.Provides

@Module
class SettingsFragmentModule {

    @Provides
    @PerFragment
    fun settingsView(frag: SettingsFragment): SettingsView {
        return frag
    }

    @Provides
    @PerFragment
    fun settingsLifecycleOwner(fragment: SettingsFragment): LifecycleOwner {
        return fragment
    }
}