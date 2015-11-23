package jp.co.crowdworks.android_meteor.rx;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import java.util.List;
import java.util.Map;

public class RxWebSocketCallback {
    public static abstract class Base {
        public String eventName;
        public WebSocket websocket;
        protected Base(String eventName, WebSocket ws) {
            this.eventName = eventName;
            this.websocket = ws;
        }
    }

    public static class StateChanged extends Base {
        public WebSocketState newState;

        public StateChanged(String eventName, WebSocket websocket, WebSocketState newState) {
            super(eventName, websocket);
            this.newState = newState;
        }
    }

    public static class Connected extends Base {
        public Map<String,List<String>> headers;

        public Connected(String eventName, WebSocket websocket, Map<String,List<String>> headers) {
            super(eventName, websocket);
            this.headers = headers;
        }
    }

    public static class ConnectError extends Base {
        public WebSocketException cause;

        public ConnectError(String eventName, WebSocket websocket, WebSocketException cause) {
            super(eventName, websocket);
            this.cause = cause;
        }
    }

    public static class Disconnected extends Base {
        public WebSocketFrame serverCloseFrame;
        public WebSocketFrame clientCloseFrame;
        public boolean closedByServer;

        public Disconnected(String eventName, WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
            super(eventName, websocket);
            this.serverCloseFrame = serverCloseFrame;
            this.clientCloseFrame = clientCloseFrame;
            this.closedByServer = closedByServer;
        }
    }

    public static class Frame extends Base {
        public WebSocketFrame frame;

        public Frame(String eventName, WebSocket websocket, WebSocketFrame frame) {
            super(eventName, websocket);
            this.frame = frame;
        }
    }

    public static class ContinuationFrame extends Base {
        public WebSocketFrame frame;

        public ContinuationFrame(String eventName, WebSocket websocket, WebSocketFrame frame) {
            super(eventName, websocket);
            this.frame = frame;
        }
    }

    public static class TextFrame extends Base {
        public WebSocketFrame frame;

        public TextFrame(String eventName, WebSocket websocket, WebSocketFrame frame) {
            super(eventName, websocket);
            this.frame = frame;
        }
    }

    public static class BinaryFrame extends Base {
        public WebSocketFrame frame;

        public BinaryFrame(String eventName, WebSocket websocket, WebSocketFrame frame) {
            super(eventName, websocket);
            this.frame = frame;
        }
    }

    public static class CloseFrame extends Base {
        public WebSocketFrame frame;

        public CloseFrame(String eventName, WebSocket websocket, WebSocketFrame frame) {
            super(eventName, websocket);
            this.frame = frame;
        }
    }

    public static class PingFrame extends Base {
        public WebSocketFrame frame;

        public PingFrame(String eventName, WebSocket websocket, WebSocketFrame frame) {
            super(eventName, websocket);
            this.frame = frame;
        }
    }

    public static class PongFrame extends Base {
        public WebSocketFrame frame;

        public PongFrame(String eventName, WebSocket websocket, WebSocketFrame frame) {
            super(eventName, websocket);
            this.frame = frame;
        }
    }

    public static class TextMessage extends Base {
        public String text;

        public TextMessage(String eventName, WebSocket websocket, String text) {
            super(eventName, websocket);
            this.text = text;
        }
    }

    public static class BinaryMessage extends Base {
        public byte[] binary;

        public BinaryMessage(String eventName, WebSocket websocket, byte[] binary) {
            super(eventName, websocket);
            this.binary = binary;
        }
    }

    public static class FrameSent extends Base {
        public WebSocketFrame frame;

        public FrameSent(String eventName, WebSocket websocket, WebSocketFrame frame) {
            super(eventName, websocket);
            this.frame = frame;
        }
    }

    public static class FrameUnsent extends Base {
        public WebSocketFrame frame;

        public FrameUnsent(String eventName, WebSocket websocket, WebSocketFrame frame) {
            super(eventName, websocket);
            this.frame = frame;
        }
    }

    public static class Error extends Base {
        public WebSocketException cause;

        public Error(String eventName, WebSocket websocket, WebSocketException cause) {
            super(eventName, websocket);
            this.cause = cause;
        }
    }

    public static class FrameError extends Base {
        public WebSocketException cause;
        public WebSocketFrame frame;

        public FrameError(String eventName, WebSocket websocket, WebSocketException cause, WebSocketFrame frame) {
            super(eventName, websocket);
            this.cause = cause;
            this.frame = frame;
        }
    }

    public static class MessageError extends Base {
        public WebSocketException cause;
        public List<WebSocketFrame> frames;

        public MessageError(String eventName, WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) {
            super(eventName, websocket);
            this.cause = cause;
            this.frames = frames;
        }
    }

    public static class TextMessageError extends Base {
        public WebSocketException cause;
        public byte[] data;

        public TextMessageError(String eventName, WebSocket websocket, WebSocketException cause, byte[] data) {
            super(eventName, websocket);
            this.cause = cause;
            this.data = data;
        }
    }

    public static class SendError extends Base {
        public WebSocketException cause;
        public WebSocketFrame frame;

        public SendError(String eventName, WebSocket websocket, WebSocketException cause, WebSocketFrame frame) {
            super(eventName, websocket);
            this.cause = cause;
            this.frame = frame;
        }
    }

    public static class UnexpectedError extends Base {
        public WebSocketException cause;

        public UnexpectedError(String eventName, WebSocket websocket, WebSocketException cause) {
            super(eventName, websocket);
            this.cause = cause;
        }
    }

}