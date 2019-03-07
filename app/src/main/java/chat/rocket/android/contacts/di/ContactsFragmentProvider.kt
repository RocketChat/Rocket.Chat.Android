package chat.rocket.android.contacts.di

import chat.rocket.android.chatrooms.di.ChatRoomsFragmentModule
import chat.rocket.android.contacts.ui.ContactsFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContactsFragmentProvider {

    @ContributesAndroidInjector(modules = [ContactsFragmentModule::class, ChatRoomsFragmentModule::class])
    @PerFragment
    abstract fun provideContactsFragment(): ContactsFragment
}