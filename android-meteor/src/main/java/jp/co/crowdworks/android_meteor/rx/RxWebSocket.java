package jp.co.crowdworks.android_meteor.rx;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import rx.Observable;
import rx.Subscriber;

public class RxWebSocket {
    private static WebSocketFactory sSocketFactory = new WebSocketFactory().setConnectionTimeout(4000);
    private WebSocket mWebSocket;

    public Observable<RxWebSocketCallback.Base> connect(final String url){
        return Observable.create(new Observable.OnSubscribe<RxWebSocketCallback.Base>() {
            @Override
            public void call(Subscriber<? super RxWebSocketCallback.Base> subscriber) {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            mWebSocket = sSocketFactory.createSocket(url);
                            mWebSocket.addListener(new RxWebSocketListener(subscriber));
                            mWebSocket.connectAsynchronously();
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                }.start();
            }
        });
    }

    public RxWebSocket sendText(String message) {
        mWebSocket.sendText(message);
        return this;
    }

    public RxWebSocket sendTextAnd(String message) {
        mWebSocket.sendText(message, false);
        return this;
    }

    public RxWebSocket setPingInterval(long intervalMs){
        mWebSocket.setPingInterval(intervalMs);
        return this;
    }

    public RxWebSocket disconnect(){
        mWebSocket.disconnect();
        return this;
    }

}
