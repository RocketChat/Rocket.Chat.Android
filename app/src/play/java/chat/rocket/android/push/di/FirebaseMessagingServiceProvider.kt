package chat.rocket.android.push.di

import chat.rocket.android.push.FirebaseMessagingService
import dagger.Module

@Module
abstract class FirebaseMessagingServiceProvider {

    abstract fun provideFirebaseMessagingService(): FirebaseMessagingService
}