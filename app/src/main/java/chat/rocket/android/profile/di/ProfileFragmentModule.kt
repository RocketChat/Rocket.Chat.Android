package chat.rocket.android.profile.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.profile.presentation.ProfileView
import chat.rocket.android.profile.ui.ProfileFragment
import dagger.Module
import dagger.Provides

@Module
class ProfileFragmentModule {

    @Provides
    @PerFragment
    fun profileView(frag: ProfileFragment): ProfileView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: ProfileFragment): LifecycleOwner {
        return frag
    }
}