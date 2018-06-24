package chat.rocket.android.dagger

import android.app.Application
import chat.rocket.android.app.RocketChatWearApplication
import chat.rocket.android.dagger.module.ActivityBuilder
import chat.rocket.android.dagger.module.AppModule
import chat.rocket.android.dagger.module.ServiceBuilder
import chat.rocket.android.services.DataLayerListenerService
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ServiceBuilder::class, ActivityBuilder::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: RocketChatWearApplication)

    fun inject(service: DataLayerListenerService)
}