package jp.co.crowdworks.android_meteor.rx;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import java.util.List;
import java.util.Map;

import rx.Subscriber;

class RxWebSocketListener implements WebSocketListener{
    private Subscriber<? super RxWebSocketCallback.Base> mSubscriber;
    public RxWebSocketListener(Subscriber<? super RxWebSocketCallback.Base> subscriber){
        mSubscriber = subscriber;
    }
    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.StateChanged("StateChanged", websocket, newState));
    }
    @Override
    public void onConnected(WebSocket websocket, Map<String,List<String>> headers) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.Connected("Connected", websocket, headers));
    }
    @Override
    public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.ConnectError("ConnectError", websocket, cause));
    }
    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.Disconnected("Disconnected", websocket, serverCloseFrame, clientCloseFrame, closedByServer));
    }
    @Override
    public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.Frame("Frame", websocket, frame));
    }
    @Override
    public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.ContinuationFrame("ContinuationFrame", websocket, frame));
    }
    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.TextFrame("TextFrame", websocket, frame));
    }
    @Override
    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.BinaryFrame("BinaryFrame", websocket, frame));
    }
    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.CloseFrame("CloseFrame", websocket, frame));
    }
    @Override
    public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.PingFrame("PingFrame", websocket, frame));
    }
    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.PongFrame("PongFrame", websocket, frame));
    }
    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.TextMessage("TextMessage", websocket, text));
    }
    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.BinaryMessage("BinaryMessage", websocket, binary));
    }
    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.FrameSent("FrameSent", websocket, frame));
    }
    @Override
    public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.FrameUnsent("FrameUnsent", websocket, frame));
    }
    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.Error("Error", websocket, cause));
    }
    @Override
    public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.FrameError("FrameError", websocket, cause, frame));
    }
    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.MessageError("MessageError", websocket, cause, frames));
    }
    @Override
    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.TextMessageError("TextMessageError", websocket, cause, data));
    }
    @Override
    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.SendError("SendError", websocket, cause, frame));
    }
    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
        mSubscriber.onNext(new RxWebSocketCallback.UnexpectedError("UnexpectedError", websocket, cause));
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        mSubscriber.onError(cause);
    }
}
