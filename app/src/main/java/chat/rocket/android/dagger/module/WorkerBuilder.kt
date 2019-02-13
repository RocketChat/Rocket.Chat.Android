package chat.rocket.android.dagger.module

import androidx.work.Worker
import chat.rocket.android.contacts.di.ContactsSyncSubComponent
import chat.rocket.android.contacts.worker.ContactsSyncWorker
import chat.rocket.android.dagger.qualifier.WorkerKey
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap


@Module(subcomponents = [ContactsSyncSubComponent::class])
abstract class WorkerBuilder {
    @Binds
    @IntoMap
    @WorkerKey(ContactsSyncWorker::class)
    abstract fun bindContactsSyncWorkerFactory(
            builder: ContactsSyncSubComponent.Builder
    ): AndroidInjector.Factory<out Worker>
}