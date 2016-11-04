package chat.rocket.android.ws;

import org.json.JSONArray;

import java.util.UUID;

import bolts.Task;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.android_ddp.DDPSubscription;
import rx.Observable;

/**
 * API for several POST actions.
 */
public class RocketChatWebSocketAPI {
    private final DDPClient mDDPClient;
    private final String mHostName;

    private RocketChatWebSocketAPI(String hostname) {
        mDDPClient = new DDPClient(OkHttpHelper.getClientForWebSocket());
        mHostName = hostname;
    }

    /**
     * create new API client instance.
     */
    public static RocketChatWebSocketAPI create(String hostname) {
        return new RocketChatWebSocketAPI(hostname);
    }

    /**
     * Connect to WebSocket server with DDP client.
     */
    public Task<DDPClientCallback.Connect> connect() {
        return mDDPClient.connect("wss://" + mHostName + "/websocket");
    }

    /**
     * Returns whether DDP client is connected to WebSocket server.
     */
    public boolean isConnected() {
        return mDDPClient.isConnected();
    }

    /**
     * close connection.
     */
    public void close() {
        mDDPClient.close();
    }


    /**
     * Subscribe with DDP client.
     */
    public Task<DDPSubscription.Ready> subscribe(final String name, JSONArray param) {
        return mDDPClient.sub(UUID.randomUUID().toString(), name, param);
    }

    /**
     * Unsubscribe with DDP client.
     */
    public Task<DDPSubscription.NoSub> unsubscribe(final String subscriptionId) {
        return mDDPClient.unsub(subscriptionId);
    }

    /**
     * Returns Observable for handling DDP subscription.
     */
    public Observable<DDPSubscription.Event> getSubscriptionCallback() {
        return mDDPClient.getSubscriptionCallback();
    }

}
