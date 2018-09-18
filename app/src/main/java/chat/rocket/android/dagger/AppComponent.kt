package chat.rocket.android.dagger

import android.app.Application
import chat.rocket.android.app.RocketChatApplication
import chat.rocket.android.chatroom.service.MessageService
import chat.rocket.android.dagger.module.ActivityBuilder
import chat.rocket.android.dagger.module.AndroidWorkerInjectionModule
import chat.rocket.android.dagger.module.AppModule
import chat.rocket.android.dagger.module.ReceiverBuilder
import chat.rocket.android.dagger.module.ServiceBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class,
    AppModule::class, ActivityBuilder::class, ServiceBuilder::class, ReceiverBuilder::class,
    AndroidWorkerInjectionModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: RocketChatApplication)

    fun inject(service: MessageService)
}
