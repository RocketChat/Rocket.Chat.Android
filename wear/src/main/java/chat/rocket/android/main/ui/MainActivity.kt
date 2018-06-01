package chat.rocket.android.main.ui

import android.app.Activity
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import chat.rocket.android.R
import chat.rocket.android.main.presentation.MainPresenter
import chat.rocket.android.main.presentation.MainView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

internal const val TOKEN_USER_ID_IDENTIFIER = "TOKEN_USER_ID"
internal const val TOKEN_AUTH_IDENTIFIER = "TOKEN_AUTH"
internal const val TOKEN_PATH = "/token"

class MainActivity : HasActivityInjector, WearableActivity(), MainView {
    @Inject
    lateinit var presenter: MainPresenter
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setAmbientEnabled()
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

}
