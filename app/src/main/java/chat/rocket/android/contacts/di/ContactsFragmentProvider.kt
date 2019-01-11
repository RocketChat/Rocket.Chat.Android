package chat.rocket.android.contacts.di

import chat.rocket.android.contacts.ContactsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContactsFragmentProvider {

	@ContributesAndroidInjector()
	abstract fun provideContactsFragment(): ContactsFragment
}