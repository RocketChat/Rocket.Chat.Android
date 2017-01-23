package chat.rocket.android_ddp.rx;

import java.io.IOException;
import chat.rocket.android.log.RCLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.OnErrorNotImplementedException;
import rx.observables.ConnectableObservable;

public class RxWebSocket {
  private OkHttpClient httpClient;
  private WebSocket webSocket;
  private boolean isConnected;

  public RxWebSocket(OkHttpClient client) {
    httpClient = client;
    isConnected = false;
  }

  public ConnectableObservable<RxWebSocketCallback.Base> connect(String url) {
    final Request request = new Request.Builder().url(url).build();

    return Observable.create(new Observable.OnSubscribe<RxWebSocketCallback.Base>() {
      @Override
      public void call(Subscriber<? super RxWebSocketCallback.Base> subscriber) {
        httpClient.newWebSocket(request, new WebSocketListener() {
          @Override
          public void onOpen(WebSocket webSocket, Response response) {
            isConnected = true;
            RxWebSocket.this.webSocket = webSocket;
            subscriber.onNext(new RxWebSocketCallback.Open(RxWebSocket.this.webSocket, response));
          }

          @Override
          public void onFailure(WebSocket webSocket, Throwable err, Response response) {
            try {
              isConnected = false;
              subscriber.onError(new RxWebSocketCallback.Failure(webSocket, err, response));
            } catch (OnErrorNotImplementedException ex) {
              RCLog.w(ex, "OnErrorNotImplementedException ignored");
            }
          }

          @Override
          public void onMessage(WebSocket webSocket, String text) {
            isConnected = true;
            subscriber.onNext(new RxWebSocketCallback.Message(webSocket, text));
          }

          @Override
          public void onClosed(WebSocket webSocket, int code, String reason) {
            isConnected = false;
            subscriber.onNext(new RxWebSocketCallback.Close(webSocket, code, reason));
            subscriber.onCompleted();
          }
        });
      }
    }).publish();
  }

  public boolean sendText(String message) throws IOException {
    return webSocket.send(message);
  }

  public boolean isConnected() {
    return isConnected;
  }

  public boolean close(int code, String reason) throws IOException {
    return webSocket.close(code, reason);
  }
}
