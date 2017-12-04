package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import chat.rocket.android.BuildConfig

import chat.rocket.android.dagger.DaggerApplicationComponent
import com.facebook.drawee.backends.pipeline.Fresco
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import timber.log.Timber
import javax.inject.Inject

class RocketChatApplication : Application(), HasActivityInjector {

    @Inject lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        DaggerApplicationComponent.builder()
                .application(this)
                .build()
                .inject(this)

        Fresco.initialize(this)

        AndroidThreeTen.init(this)
        setupTimber()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return activityInjector
    }
}