package chat.rocket.android.mentions.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.mentions.presentention.MentionsView
import chat.rocket.android.mentions.ui.MentionsFragment
import dagger.Module
import dagger.Provides

@Module
class MentionsFragmentModule {

    @Provides
    @PerFragment
    fun provideMentionsView(frag: MentionsFragment): MentionsView {
        return frag
    }
}