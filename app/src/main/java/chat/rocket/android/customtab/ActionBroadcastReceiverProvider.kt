package chat.rocket.android.customtab

import chat.rocket.android.dagger.module.AppModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActionBroadcastReceiverProvider {
    @ContributesAndroidInjector(modules = [AppModule::class])
    abstract fun provideActionBroadcastReceiver(): ActionBroadcastReceiver
}