package chat.rocket.android_ddp.rx;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.flowables.ConnectableFlowable;

import java.io.IOException;
import chat.rocket.android.log.RCLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RxWebSocket {
  private OkHttpClient httpClient;
  private WebSocket webSocket;

  public RxWebSocket(OkHttpClient client) {
    httpClient = client;
  }

  public ConnectableFlowable<RxWebSocketCallback.Base> connect(String url) {
    final Request request = new Request.Builder().url(url).build();

    return Flowable.create(
        new FlowableOnSubscribe<RxWebSocketCallback.Base>() {
          @Override
          public void subscribe(FlowableEmitter<RxWebSocketCallback.Base> emitter)
              throws Exception {
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
                emitter.onComplete();
              }
            });
          }
        }, BackpressureStrategy.BUFFER
    ).publish();
  }

  public boolean sendText(String message) throws IOException {
    return webSocket.send(message);
  }

  public boolean close(int code, String reason) throws IOException {
    return webSocket.close(code, reason);
  }
}
