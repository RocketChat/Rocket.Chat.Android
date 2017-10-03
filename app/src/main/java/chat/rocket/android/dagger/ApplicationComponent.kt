package chat.rocket.android.dagger

import android.app.Application

import javax.inject.Singleton

import chat.rocket.android.app.RocketChatApplication
import chat.rocket.android.dagger.module.ActivityBindingModule
import chat.rocket.android.dagger.module.ApplicationModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule

@Singleton
@Component(modules = arrayOf(ActivityBindingModule::class, ApplicationModule::class,
        AndroidSupportInjectionModule::class))
interface ApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }

    fun inject(application: RocketChatApplication)
}
