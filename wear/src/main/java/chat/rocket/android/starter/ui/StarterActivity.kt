package chat.rocket.android.starter.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.starter.presentation.StarterActivityPresenter
import chat.rocket.android.starter.presentation.StarterActivityView
import chat.rocket.android.util.AppPreferenceManager
import chat.rocket.android.util.Constants.KEY_PREFS_ACTIVITY_FOREGROUND
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class StarterActivity : HasActivityInjector, WearableActivity(),
    StarterActivityView {
    private lateinit var sharedPreferencesManager: AppPreferenceManager
    @Inject
    lateinit var presenter: StarterActivityPresenter
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starter)
        setAmbientEnabled()
        // Toast.makeText(this, )
        sharedPreferencesManager = AppPreferenceManager(this)
        presenter.saveCredentials()
        checkIfLoginTokensExist()
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    override fun onResume() {
        super.onResume()
        sharedPreferencesManager.editSharedPreference(KEY_PREFS_ACTIVITY_FOREGROUND, true)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferencesManager.editSharedPreference(KEY_PREFS_ACTIVITY_FOREGROUND, false)
    }

    override fun onStop() {
        super.onStop()
        sharedPreferencesManager.editSharedPreference(KEY_PREFS_ACTIVITY_FOREGROUND, false)
    }

    override fun onStart() {
        super.onStart()
        sharedPreferencesManager.editSharedPreference(KEY_PREFS_ACTIVITY_FOREGROUND, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferencesManager.editSharedPreference(KEY_PREFS_ACTIVITY_FOREGROUND, false)
    }

    private fun checkIfLoginTokensExist() {
        presenter.loadCredentials { authenticated ->
            if (authenticated) {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }
}
