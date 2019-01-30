package chat.rocket.android.dagger.module

import androidx.work.Worker
import chat.rocket.android.contacts.di.ContactSyncSubComponent
import chat.rocket.android.contacts.worker.ContactSyncWorker
import chat.rocket.android.dagger.qualifier.WorkerKey
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap


@Module(subcomponents = [ContactSyncSubComponent::class])
abstract class WorkerBuilder {
    @Binds
    @IntoMap
    @WorkerKey(ContactSyncWorker::class)
    abstract fun bindContactSyncWorkerFactory(
            builder: ContactSyncSubComponent.Builder
    ): AndroidInjector.Factory<out Worker>
}