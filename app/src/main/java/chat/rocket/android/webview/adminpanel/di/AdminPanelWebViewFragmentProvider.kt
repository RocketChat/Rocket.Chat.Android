package chat.rocket.android.webview.adminpanel.di

import chat.rocket.android.webview.adminpanel.ui.AdminPanelWebViewFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AdminPanelWebViewFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideAdminPanelWebViewFragment(): AdminPanelWebViewFragment
}