package jp.co.crowdworks.android_ddp.rx;

import android.util.Log;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;

import java.io.IOException;

import okio.Buffer;

public class RxWebSocketCallback {
    public static abstract class Base{
        public String type;
        public WebSocket ws;
        public Base(String type, WebSocket ws){
            this.type = type;
            this.ws = ws;
        }

        @Override
        public String toString() {
            return "["+type+"]";
        }
    }

    public static class Open extends Base {
        public Response response;

        public Open(WebSocket websocket, Response response) {
            super("Open", websocket);
            this.response = response;
        }
    }

    public static class Failure extends Base {
        public IOException e;
        public Response response;

        public Failure(WebSocket websocket, IOException e, Response response) {
            super("Failure", websocket);
            this.e = e;
            this.response = response;
        }

        @Override
        public String toString() {
            if (response!=null) return "["+type+"] "+response.message();
            else return super.toString();
        }
    }

    public static class Message extends Base {
        public String responseBodyString;

        public Message(WebSocket websocket, ResponseBody responseBody) {
            super("Message", websocket);
            try {
                this.responseBodyString = responseBody.string();
            } catch(Exception e){
                Log.e(RxWebSocket.TAG, "error in reading response(Message)", e);
            }
        }

        @Override
        public String toString() {
            return "["+type+"] "+responseBodyString;
        }
    }

    public static class Pong extends Base {
        public Buffer payload;

        public Pong(WebSocket websocket, Buffer payload) {
            super("Pong", websocket);
            this.payload = payload;
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
            return "["+type+"] code="+code+", reason="+reason;
        }
    }


}
