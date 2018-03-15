package chat.rocket.android.chatroom.information.di

import chat.rocket.android.chatroom.information.ui.InformationFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class InformationFragmentProvider {
    @ContributesAndroidInjector(modules = [InformationFragmentModule::class])
    abstract fun provideInformationFragment(): InformationFragment
}

