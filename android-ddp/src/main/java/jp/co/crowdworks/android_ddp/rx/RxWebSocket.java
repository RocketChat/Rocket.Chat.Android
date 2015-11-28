package jp.co.crowdworks.android_ddp.rx;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import java.io.IOException;

import okio.Buffer;
import rx.Observable;
import rx.Subscriber;
import rx.observables.ConnectableObservable;

public class RxWebSocket {
    static final String TAG = "RxWebSocket";

    private OkHttpClient mHttpClient;
    private WebSocket mWebSocket;

    public RxWebSocket(OkHttpClient client) {
        mHttpClient = client;
    }
    public ConnectableObservable<RxWebSocketCallback.Base> connect(String url){
        final Request request = new Request.Builder().url(url).build();
        WebSocketCall call = WebSocketCall.create(mHttpClient, request);

        return Observable.create(new Observable.OnSubscribe<RxWebSocketCallback.Base>() {
            @Override
            public void call(Subscriber<? super RxWebSocketCallback.Base> subscriber) {
                call.enqueue(new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        mWebSocket = webSocket;
                        subscriber.onNext(new RxWebSocketCallback.Open(mWebSocket, response));
                    }

                    @Override
                    public void onFailure(IOException e, Response response) {
                        subscriber.onNext(new RxWebSocketCallback.Failure(mWebSocket, e, response));
                    }

                    @Override
                    public void onMessage(ResponseBody responseBody) throws IOException {
                        subscriber.onNext(new RxWebSocketCallback.Message(mWebSocket, responseBody));
                    }

                    @Override
                    public void onPong(Buffer payload) {
                        subscriber.onNext(new RxWebSocketCallback.Pong(mWebSocket, payload));
                    }

                    @Override
                    public void onClose(int code, String reason) {
                        subscriber.onNext(new RxWebSocketCallback.Close(mWebSocket, code, reason));
                    }
                });
            }
        }).publish();
    }


    public void sendText(String message) throws IOException {
        mWebSocket.sendMessage(RequestBody.create(WebSocket.TEXT, message));
    }
}
