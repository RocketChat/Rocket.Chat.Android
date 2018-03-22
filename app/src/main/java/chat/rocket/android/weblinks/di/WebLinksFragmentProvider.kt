package chat.rocket.android.weblinks.di

import chat.rocket.android.weblinks.ui.WebLinksFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class WebLinksFragmentProvider {

    @ContributesAndroidInjector(modules = [WebLinksFragmentModule::class])
    abstract fun provideWebLinksFragment(): WebLinksFragment
}