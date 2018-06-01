package chat.rocket.android

import android.net.Uri
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import java.util.concurrent.TimeUnit


class DataLayerListenerService: WearableListenerService() {
    override fun onCreate() {
        super.onCreate()

    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer?) {
        //super.onDataChanged(dataEventBuffer)

        // Loop through the events and send a message back to the node that created the data item.
        if (dataEventBuffer != null) {
            for (event in dataEventBuffer) {
                val uri: Uri = event.dataItem.uri
                val path: String = uri.path
                if ("/token" == path) {
                    // Get the node id of the node that created the data item from the host portion of
                    // the uri.
                    val nodeId = uri.getHost()
                    // Set the data of the message to be the bytes of the Uri.
                    val payload = uri.toString().toByteArray()

                    // Send the rpc
    //                Wearable.MessageApi.sendMessage(
    //                    mGoogleApiClient, nodeId, DATA_ITEM_RECEIVED_PATH,
    //                    payload
    //                )
                }
            }
        }

    }
}