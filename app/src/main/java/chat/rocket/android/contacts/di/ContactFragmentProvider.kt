package chat.rocket.android.contacts.di

import chat.rocket.android.contacts.ContactsFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContactFragmentProvider {

    @ContributesAndroidInjector()
    @PerFragment
    abstract fun provideContactFragment(): ContactsFragment
}