package chat.rocket.android.profile.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.profile.presentation.ProfileView
import chat.rocket.android.profile.ui.ProfileFragment
import dagger.Module
import dagger.Provides

@Module
@PerFragment
class ProfileFragmentModule {

    @Provides
    fun profileView(frag: ProfileFragment): ProfileView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: ProfileFragment): LifecycleOwner {
        return frag
    }
}