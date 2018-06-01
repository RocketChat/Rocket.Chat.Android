package chat.rocket.android

import android.net.Uri
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService


class DataLayerListenerService : WearableListenerService() {

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