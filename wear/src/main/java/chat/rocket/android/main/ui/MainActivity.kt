package chat.rocket.android.main.ui

import android.app.Activity
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.main.presentation.MainPresenter
import chat.rocket.android.main.presentation.MainView
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
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

        // Enables Always-on
        setAmbientEnabled()
    }

    override fun onPause() {
        super.onPause()
        //Wearable.getDataClient(this).removeListener(this)
    }

    override fun onResume() {
        super.onResume()
        //Wearable.getDataClient(this).addListener(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

//    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
//        for (dataEvent in dataEventBuffer) {
//            val path = dataEvent.dataItem.uri.path
//            if (path == TOKEN_PATH) {
//                val dataMap = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
//                Toast.makeText(
//                    this,
//                    "userId is ${dataMap.getString(TOKEN_USER_ID_IDENTIFIER)}",
//                    Toast.LENGTH_SHORT
//                ).show()
//                Toast.makeText(
//                    this,
//                    "AuthToken is ${dataMap.getString(TOKEN_AUTH_IDENTIFIER)}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//
//        }
//
//    }
}
