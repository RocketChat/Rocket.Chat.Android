package chat.rocket.android.push.di

import chat.rocket.android.dagger.module.AppModule
import chat.rocket.android.push.FirebaseTokenService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class FirebaseTokenServiceProvider {
    @ContributesAndroidInjector(modules = [AppModule::class])
    abstract fun provideFirebaseTokenService(): FirebaseTokenService
}