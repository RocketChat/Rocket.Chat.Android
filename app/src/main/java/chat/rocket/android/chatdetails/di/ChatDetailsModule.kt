package chat.rocket.android.chatdetails.di

import chat.rocket.android.chatdetails.presentation.ChatDetailsNavigator
import chat.rocket.android.chatdetails.ui.ChatDetailsActivity
import chat.rocket.android.dagger.scope.PerActivity
import dagger.Module
import dagger.Provides

@Module
class ChatDetailsModule {
    @Provides
    @PerActivity
    fun providesNavigator(activity: ChatDetailsActivity) = ChatDetailsNavigator(activity)
}