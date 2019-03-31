package chat.rocket.android.privacy.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.privacy.presentation.PrivacyView
import chat.rocket.android.privacy.ui.PrivacyFragment
import dagger.Module
import dagger.Provides

@Module
class PrivacyFragmentModule {

    @Provides
    @PerFragment
    fun privacyView(frag: PrivacyFragment): PrivacyView {
        return frag
    }
}