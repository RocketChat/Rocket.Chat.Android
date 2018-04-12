package chat.rocket.android.chatroom.edit.di

import chat.rocket.android.chatroom.edit.ui.EditInfoFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class EditInfoFragmentProvider {
    @ContributesAndroidInjector(modules = [EditInfoFragmentModule::class])
    abstract fun provideEditInfoFragment(): EditInfoFragment
}