package chat.rocket.android.widget.share.di

import chat.rocket.android.widget.share.ui.ShareBottomSheetDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ShareFragmentProvider {

    @ContributesAndroidInjector(modules = [ShareFragmentModule::class])
    abstract fun provideShareFragment(): ShareBottomSheetDialog
}