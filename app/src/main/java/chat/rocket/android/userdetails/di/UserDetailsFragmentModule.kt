package chat.rocket.android.userdetails.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.userdetails.presentation.UserDetailsView
import chat.rocket.android.userdetails.ui.UserDetailsFragment
import dagger.Module
import dagger.Provides

@Module
class UserDetailsFragmentModule {

    @Provides
    @PerFragment
    fun provideUserDetailsView(frag: UserDetailsFragment): UserDetailsView = frag

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: UserDetailsFragment): LifecycleOwner = frag
}
