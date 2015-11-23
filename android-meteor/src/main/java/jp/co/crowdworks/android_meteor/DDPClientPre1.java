package jp.co.crowdworks.android_meteor;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import jp.co.crowdworks.android_meteor.rx.RxWebSocket;
import jp.co.crowdworks.android_meteor.rx.RxWebSocketCallback;
import rx.Observable;
import rx.Subscription;

public class DDPClientPre1 {
    private static final String TAG = "DDP";
    // reference: https://github.com/eddflrs/meteor-ddp/blob/master/meteor-ddp.js
    private RxWebSocket mWebSocket = new RxWebSocket();
    private Observable<RxWebSocketCallback.Base> mConnection;


    public void connect(final String url){
        mConnection = mWebSocket.connect(url);

        Subscription s = mConnection
                .filter(callback -> callback instanceof RxWebSocketCallback.Connected)
                .first()
                .subscribe(callback -> {
                    mWebSocket.setPingInterval(40 * 1000);

                    sendJSON("connect", (json) ->
                                    json.put("version", "pre1")
                    );
                });

        Subscription s2 = mConnection
                .filter(callback -> callback instanceof RxWebSocketCallback.TextMessage)
                .subscribe(callback -> {
                    Log.d(TAG, callback.eventName+": "+((RxWebSocketCallback.TextMessage) callback).text);
                });
    }

    private interface JSONBuilder {
        JSONObject create(JSONObject root) throws JSONException;
    }

    private void sendJSON(String msg, JSONBuilder json) {
        try {
            mWebSocket.sendText(json.create(new JSONObject().put("msg", msg)).toString());
        }
        catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
        }
    }
}
