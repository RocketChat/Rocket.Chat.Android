package chat.rocket.android.mentions.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.mentions.ui.MentionsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MentionsFragmentProvider {

    @ContributesAndroidInjector(modules = [MentionsFragmentModule::class])
    @PerFragment
    abstract fun provideMentionsFragment(): MentionsFragment
}