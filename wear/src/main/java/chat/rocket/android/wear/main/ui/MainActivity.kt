package chat.rocket.android.wear.main.ui

import android.app.Activity
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import chat.rocket.android.wear.R
import chat.rocket.android.wear.main.presentation.MainPresenter
import chat.rocket.android.wear.main.presentation.MainView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class MainActivity : HasActivityInjector, WearableActivity(), MainView {

    @Inject
    lateinit var presenter: MainPresenter
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector
}
