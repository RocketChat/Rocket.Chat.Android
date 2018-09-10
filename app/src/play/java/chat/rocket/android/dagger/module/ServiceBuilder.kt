package chat.rocket.android.dagger.module

import androidx.work.Worker
import chat.rocket.android.chatroom.di.MessageServiceProvider
import chat.rocket.android.chatroom.service.MessageService
import chat.rocket.android.dagger.qualifier.WorkerKey
import chat.rocket.android.push.FirebaseMessagingService
import chat.rocket.android.push.di.FirebaseMessagingServiceProvider
import chat.rocket.android.push.di.TokenRegistrationSubComponent
import chat.rocket.android.push.worker.TokenRegistrationWorker
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(subcomponents = [TokenRegistrationSubComponent::class])
abstract class ServiceBuilder {

    @ContributesAndroidInjector(modules = [FirebaseMessagingServiceProvider::class])
    abstract fun bindGcmListenerService(): FirebaseMessagingService

    @ContributesAndroidInjector(modules = [MessageServiceProvider::class])
    abstract fun bindMessageService(): MessageService

    @Binds
    @IntoMap
    @WorkerKey(TokenRegistrationWorker::class)
    abstract fun bindTokenRegistrationWorkerFactory(
        builder: TokenRegistrationSubComponent.Builder
    ): AndroidInjector.Factory<out Worker>
}