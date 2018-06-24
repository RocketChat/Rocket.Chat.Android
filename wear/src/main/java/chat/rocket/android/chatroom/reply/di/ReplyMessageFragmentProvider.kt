package chat.rocket.android.chatroom.reply.di

import chat.rocket.android.chatroom.reply.ui.ReplyMessageFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ReplyMessageFragmentProvider {
    @ContributesAndroidInjector(modules = [ReplyMessageFragmentModule::class])
    @PerFragment
    abstract fun provideReplyMessageFragment(): ReplyMessageFragment
}