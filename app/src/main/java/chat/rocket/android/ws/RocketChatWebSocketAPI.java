package chat.rocket.android.ws;

import bolts.Task;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;

public class RocketChatWebSocketAPI {
    private final DDPClient mDDPClient;
    private final String mHostName;

    private RocketChatWebSocketAPI(String hostname) {
        mDDPClient = new DDPClient(OkHttpHelper.getClientForWebSocket());
        mHostName = hostname;
    }

    public static RocketChatWebSocketAPI create(String hostname) {
        return new RocketChatWebSocketAPI(hostname);
    }

    public Task<DDPClientCallback.Connect> connect() {
        return mDDPClient.connect("wss://" + mHostName + "/websocket");
    }

    public boolean isConnected() {
        return mDDPClient.isConnected();
    }

    public void close() {
        mDDPClient.close();
    }
}
