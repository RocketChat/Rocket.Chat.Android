package chat.rocket.android_ddp.rx;

import java.io.IOException;
import chat.rocket.android.log.RCLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import rx.Emitter;
import rx.Observable;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;

public class RxWebSocket {
  private OkHttpClient httpClient;
  private WebSocket webSocket;

  public RxWebSocket(OkHttpClient client) {
    httpClient = client;
  }

  public ConnectableObservable<RxWebSocketCallback.Base> connect(String url) {
    final Request request = new Request.Builder().url(url).build();

    return Observable.fromEmitter(
        new Action1<Emitter<RxWebSocketCallback.Base>>() {
          @Override
          public void call(Emitter<RxWebSocketCallback.Base> emitter) {
            httpClient.newWebSocket(request, new WebSocketListener() {
              @Override
              public void onOpen(WebSocket webSocket, Response response) {
                RxWebSocket.this.webSocket = webSocket;
                emitter.onNext(new RxWebSocketCallback.Open(RxWebSocket.this.webSocket, response));
              }

              @Override
              public void onFailure(WebSocket webSocket, Throwable err, Response response) {
                try {
                  emitter.onError(new RxWebSocketCallback.Failure(webSocket, err, response));
                } catch (OnErrorNotImplementedException ex) {
                  RCLog.w(ex, "OnErrorNotImplementedException ignored");
                }
              }

              @Override
              public void onMessage(WebSocket webSocket, String text) {
                emitter.onNext(new RxWebSocketCallback.Message(webSocket, text));
              }

              @Override
              public void onClosed(WebSocket webSocket, int code, String reason) {
                emitter.onNext(new RxWebSocketCallback.Close(webSocket, code, reason));
                emitter.onCompleted();
              }
            });
          }
        },
        Emitter.BackpressureMode.BUFFER
    ).publish();
  }

  public boolean sendText(String message) throws IOException {
    return webSocket.send(message);
  }

  public boolean close(int code, String reason) throws IOException {
    return webSocket.close(code, reason);
  }
}
