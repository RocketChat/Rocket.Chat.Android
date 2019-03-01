package chat.rocket.android.contacts.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.contacts.presentation.ContactsView
import chat.rocket.android.contacts.ui.ContactsFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
class ContactsFragmentModule {

    @Provides
    @PerFragment
    fun contactsView(frag: ContactsFragment): ContactsView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: ContactsFragment): LifecycleOwner {
        return frag
    }
}