package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.SaveCurrentServerInteractor
import chat.rocket.android.server.TokenRepository
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class RocketChatWearApplication : Application(), HasActivityInjector {
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    @Inject
    lateinit var getCurrentServerInteractor: GetCurrentServerInteractor

    @Inject
    lateinit var saveCurrentServerRepository: SaveCurrentServerInteractor

    @Inject
    lateinit var tokenRepository: TokenRepository

    @Inject
    lateinit var prefs: SharedPreferences
}
