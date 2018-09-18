package chat.rocket.android.push.di

import chat.rocket.android.push.worker.TokenRegistrationWorker
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface TokenRegistrationSubComponent : AndroidInjector<TokenRegistrationWorker> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<TokenRegistrationWorker>()
}