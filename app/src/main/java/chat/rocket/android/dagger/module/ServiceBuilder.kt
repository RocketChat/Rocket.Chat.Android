package chat.rocket.android.dagger.module

import chat.rocket.android.push.FirebaseTokenService
import chat.rocket.android.push.di.FirebaseTokenServiceProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class ServiceBuilder {

    @ContributesAndroidInjector(modules = [FirebaseTokenServiceProvider::class])
    abstract fun bindFirebaseTokenService(): FirebaseTokenService
}