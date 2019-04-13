package chat.rocket.android.push

import dagger.Module

@Module
abstract class DirectReplyReceiverProvider {

    abstract fun provideDirectReplyReceiver(): DirectReplyReceiver
}