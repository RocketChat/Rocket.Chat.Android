package chat.rocket.android_ddp.rx;

import chat.rocket.android.log.RCLog;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
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
    WebSocketCall call = WebSocketCall.create(httpClient, request);

    return Observable.create(new Observable.OnSubscribe<RxWebSocketCallback.Base>() {
      @Override public void call(Subscriber<? super RxWebSocketCallback.Base> subscriber) {
        call.enqueue(new WebSocketListener() {
          @Override public void onOpen(WebSocket webSocket, Response response) {
            isConnected = true;
            RxWebSocket.this.webSocket = webSocket;
            subscriber.onNext(new RxWebSocketCallback.Open(RxWebSocket.this.webSocket, response));
          }

          @Override public void onFailure(IOException e, Response response) {
            try {
              isConnected = false;
              subscriber.onError(new RxWebSocketCallback.Failure(webSocket, e, response));
            } catch (OnErrorNotImplementedException ex) {
              RCLog.w(ex, "OnErrorNotImplementedException ignored");
            }
          }

          @Override public void onMessage(ResponseBody responseBody) throws IOException {
            isConnected = true;
            subscriber.onNext(new RxWebSocketCallback.Message(webSocket, responseBody));
          }

          @Override public void onPong(Buffer payload) {
            isConnected = true;
            subscriber.onNext(new RxWebSocketCallback.Pong(webSocket, payload));
          }

          @Override public void onClose(int code, String reason) {
            isConnected = false;
            subscriber.onNext(new RxWebSocketCallback.Close(webSocket, code, reason));
            subscriber.onCompleted();
          }
        });
      }
    }).publish();
  }

  public void sendText(String message) throws IOException {
    webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, message));
  }

  public boolean isConnected() {
    return isConnected;
  }

  public void close(int code, String reason) throws IOException {
    webSocket.close(code, reason);
  }
}
