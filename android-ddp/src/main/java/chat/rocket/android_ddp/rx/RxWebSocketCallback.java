package chat.rocket.android_ddp.rx;

import static android.R.attr.type;

import chat.rocket.android.log.RCLog;
import okhttp3.Response;
import okhttp3.WebSocket;

public class RxWebSocketCallback {
  public static abstract class Base {
    public String type;
    public WebSocket ws;

    public Base(String type, WebSocket ws) {
      this.type = type;
      this.ws = ws;
    }

    @Override
    public String toString() {
      return "[" + type + "]";
    }
  }

  public static class Open extends Base {
    public Response response;

    public Open(WebSocket websocket, Response response) {
      super("Open", websocket);
      this.response = response;
    }
  }

  public static class Failure extends Exception {
    public WebSocket ws;
    public Response response;

    public Failure(WebSocket websocket, Throwable err, Response response) {
      super(err);
      this.ws = websocket;
      this.response = response;
    }

    @Override
    public String toString() {
      if (response != null) {
        return "[" + type + "] " + response.message();
      } else {
        return super.toString();
      }
    }
  }

  public static class Message extends Base {
    public String responseBodyString;

    public Message(WebSocket websocket, String responseBody) {
      super("Message", websocket);
      try {
        this.responseBodyString = responseBody;
      } catch (Exception e) {
        RCLog.e(e, "error in reading response(Message)");
      }
    }

    @Override
    public String toString() {
      return "[" + type + "] " + responseBodyString;
    }
  }

  public static class Close extends Base {
    public int code;
    public String reason;

    public Close(WebSocket websocket, int code, String reason) {
      super("Close", websocket);
      this.code = code;
      this.reason = reason;
    }

    @Override
    public String toString() {
      return "[" + type + "] code=" + code + ", reason=" + reason;
    }
  }
}
