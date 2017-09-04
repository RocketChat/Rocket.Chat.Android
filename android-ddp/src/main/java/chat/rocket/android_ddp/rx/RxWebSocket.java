package chat.rocket.android_ddp.rx;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import chat.rocket.android.log.RCLog;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.flowables.ConnectableFlowable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RxWebSocket {
  public static final int REASON_NETWORK_ERROR = 100;
  private OkHttpClient httpClient;
  private WebSocket webSocket;
  private boolean hadErrorsBefore;

  public RxWebSocket(OkHttpClient client) {
    httpClient = client;
  }

  public ConnectableFlowable<RxWebSocketCallback.Base> connect(String url) {
    final Request request = new Request.Builder().url(url).build();

    return Flowable.create(
        (FlowableOnSubscribe<RxWebSocketCallback.Base>) emitter -> httpClient
            .newWebSocket(request, new WebSocketListener() {
              @Override
              public void onOpen(WebSocket webSocket, Response response) {
                hadErrorsBefore = false;
                RxWebSocket.this.webSocket = webSocket;
                emitter.onNext(new RxWebSocketCallback.Open(RxWebSocket.this.webSocket, response));
              }

              @Override
              public void onFailure(WebSocket webSocket, Throwable err, Response response) {
                try {
                  if (!hadErrorsBefore) {
                    hadErrorsBefore = true;
                    emitter.onNext(new RxWebSocketCallback.Close(webSocket, REASON_NETWORK_ERROR, err.getMessage()));
                    emitter.onComplete();
                  }
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
              }
            }),
        BackpressureStrategy.BUFFER
    ).publish();
  }

  public boolean sendText(String message) throws IOException {
    if (webSocket == null) {
        return false;
    }
    return webSocket.send(message);
  }

  public boolean close(int code, String reason) throws IOException {
    if (webSocket == null) {
      return false;
    }
    return webSocket.close(code, reason);
  }
}
