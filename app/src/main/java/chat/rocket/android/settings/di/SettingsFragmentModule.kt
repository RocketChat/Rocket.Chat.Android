package chat.rocket.android.settings.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.settings.presentation.SettingsView
import chat.rocket.android.settings.ui.SettingsFragment
import dagger.Module
import dagger.Provides

@Module
@PerFragment
class SettingsFragmentModule {

    @Provides
    fun settingsView(frag: SettingsFragment): SettingsView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: SettingsFragment): LifecycleOwner {
        return frag
    }
}