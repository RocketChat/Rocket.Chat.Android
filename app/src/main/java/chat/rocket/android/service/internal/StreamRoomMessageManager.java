package chat.rocket.android.service.internal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.service.Registerable;
import chat.rocket.android.service.ddp.stream.StreamRoomMessage;

/**
 * wrapper for managing stream-notify-message depending on RocketChatCache.
 */
public class StreamRoomMessageManager implements Registerable {
  private StreamRoomMessage streamRoomMessage;

  private final Context context;
  private final RealmHelper realmHelper;
  private final DDPClientWraper ddpClient;
  private final AbstractRocketChatCacheObserver cacheObserver;
  private final Handler handler;

  public StreamRoomMessageManager(Context context, RealmHelper realmHelper,
      DDPClientWraper ddpClient) {
    this.context = context;
    this.realmHelper = realmHelper;
    this.ddpClient = ddpClient;

    cacheObserver = new AbstractRocketChatCacheObserver(context, realmHelper) {
      @Override protected void onRoomIdUpdated(String roomId) {
        unregisterStreamNotifyMessageIfNeeded();
        registerStreamNotifyMessage(roomId);
      }
    };
    handler = new Handler(Looper.myLooper());
  }

  private void registerStreamNotifyMessage(String roomId) {
    streamRoomMessage = new StreamRoomMessage(context, realmHelper, ddpClient, roomId);
    handler.post(() -> {
      streamRoomMessage.register();
    });
  }

  private void unregisterStreamNotifyMessageIfNeeded() {
    if (streamRoomMessage != null) {
      handler.post(() -> {
        streamRoomMessage.unregister();
        streamRoomMessage = null;
      });
    }
  }

  @Override public void register() {
    cacheObserver.register();
  }

  @Override public void unregister() {
    unregisterStreamNotifyMessageIfNeeded();
    cacheObserver.unregister();
  }
}
