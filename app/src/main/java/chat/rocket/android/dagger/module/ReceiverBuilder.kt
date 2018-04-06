package chat.rocket.android.dagger.module

import chat.rocket.android.push.DeleteReceiver
import chat.rocket.android.push.DirectReplyReceiver
import chat.rocket.android.push.DirectReplyReceiverProvider
import chat.rocket.android.push.di.DeleteReceiverProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ReceiverBuilder {

    @ContributesAndroidInjector(modules = [DeleteReceiverProvider::class])
    abstract fun bindDeleteReceiver(): DeleteReceiver

    @ContributesAndroidInjector(modules = [DirectReplyReceiverProvider::class])
    abstract fun bindDirectReplyReceiver(): DirectReplyReceiver
}