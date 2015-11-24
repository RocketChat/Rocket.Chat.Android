package jp.co.crowdworks.android_meteor;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jp.co.crowdworks.android_meteor.rx.RxWebSocket;
import jp.co.crowdworks.android_meteor.rx.RxWebSocketCallback;
import rx.Observable;

public class DDPClient {
    private static final String TAG = "DDP";
    // reference: https://github.com/eddflrs/meteor-ddp/blob/master/meteor-ddp.js

    private RxWebSocket mWebSocket;
    private Observable<RxWebSocketCallback.Base> mObservable;
    private String mSession;

    public DDPClient(OkHttpClient client) {
        mWebSocket = new RxWebSocket(client);
    }

    public void setSession(String session) {
        mSession = session;
    }

    public void connect(final String url){
        mObservable = mWebSocket.connect(url).autoConnect();

        mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Open)
                .subscribe(callback -> {
                    sendJSON("connect", json -> json
                            .put("version", "pre1")
                            .put("support", new JSONArray()
                                    .put("pre1")
                                    //.put("pre2")
                                    //.put("1")
                            ));
                });

        mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Message)
                .map(callback -> {
                    try {
                        return ((RxWebSocketCallback.Message) callback).responseBody.string();
                    } catch (Exception e) {
                        Log.d(TAG,"error in getting response body",e);
                    }
                    return null;
                })
                .map(s -> {
                    try {
                        return s == null ? null : new JSONObject(s);
                    } catch (JSONException e) {
                        Log.d(TAG, "error in converting json: "+s, e);
                    }
                    return null;
                })
                .subscribe(json -> {
                    try {
                        Log.d(TAG, json.toString(1));
                        //handleMessageJSON(json);
                    } catch (JSONException e) {
                    }
                });

        // just for debugging.
        mObservable.subscribe(callback -> {
            Log.d(TAG, "DEBUG> "+callback);
        });
    }

    private void handleMessageJSON(JSONObject response) throws JSONException {
        final String msg = response.getString("msg");
    }

    private interface JSONBuilder {
        JSONObject create(JSONObject root) throws JSONException;
    }

    private void sendJSON(String msg, JSONBuilder json) {
        try {
            mWebSocket.sendText(json.create(new JSONObject().put("msg", msg)).toString());
        }
        catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }
}
