package chat.rocket.android.chatroom.reply.di

import chat.rocket.android.chatroom.reply.presentation.ReplyMessageView
import chat.rocket.android.chatroom.reply.ui.ReplyMessageFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
class ReplyMessageFragmentModule {
    @Provides
    @PerFragment
    fun replyMessageView(frag: ReplyMessageFragment): ReplyMessageView {
        return frag
    }
}