package chat.rocket.android.dagger.module

import chat.rocket.android.push.DirectReplyReceiver
import chat.rocket.android.push.di.DirectReplyReceiverProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ReceiverBuilder {

    @ContributesAndroidInjector(modules = [DirectReplyReceiverProvider::class])
    abstract fun bindDirectReplyReceiver(): DirectReplyReceiver
}