package chat.rocket.android.services

import android.net.Uri
import chat.rocket.android.server.GetCurrentServerInteractor
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService
import dagger.android.AndroidInjection
import javax.inject.Inject


class DataLayerListenerService : WearableListenerService() {

    @Inject
    lateinit var getCurrentServerInteractor: GetCurrentServerInteractor

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer?) {
        if (dataEventBuffer != null) {
            for (event in dataEventBuffer) {
                val uri: Uri = event.dataItem.uri
                val path: String = uri.path
                if ("/token" == path) {
                    //token received
                }
            }
        }

    }
}