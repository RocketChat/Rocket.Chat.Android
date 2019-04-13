package chat.rocket.android.push.di

import chat.rocket.android.push.DeleteReceiver
import dagger.Module

@Module
abstract class DeleteReceiverProvider {

    abstract fun provideDeleteReceiver(): DeleteReceiver
}