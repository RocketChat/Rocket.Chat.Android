package chat.rocket.android.thememanager.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.server.infrastructure.ConnectionManager
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.thememanager.model.ThemesRepository
import chat.rocket.android.thememanager.ui.ThemesFragment
import chat.rocket.android.thememanager.viewmodel.ThemesViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ThemesFragmentModule {

//    @Provides
//    @PerFragment
//    fun provideThemesViewModelFactory(
//            repository: ThemesRepository
//    ): ThemesViewModelFactory{
//        return ThemesViewModelFactory(repository)
//    }

}