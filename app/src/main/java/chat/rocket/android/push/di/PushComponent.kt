package chat.rocket.android.push.di

import chat.rocket.android.dagger.module.AppModule
import chat.rocket.android.push.FirebaseTokenService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [PushModule::class])
interface PushComponent {
    fun inject(service: FirebaseTokenService)
}