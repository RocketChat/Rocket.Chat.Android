package chat.rocket.android.contacts.di
import chat.rocket.android.contacts.worker.ContactSyncWorker
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ContactSyncSubComponent : AndroidInjector<ContactSyncWorker> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ContactSyncWorker>()
}