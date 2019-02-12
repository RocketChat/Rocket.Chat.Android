package chat.rocket.android.contacts.di
import chat.rocket.android.contacts.worker.ContactsSyncWorker
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ContactsSyncSubComponent : AndroidInjector<ContactsSyncWorker> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ContactsSyncWorker>()
}