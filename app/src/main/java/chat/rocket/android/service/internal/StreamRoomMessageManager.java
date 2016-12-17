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
  private final Context context;
  private final String hostname;
  private final RealmHelper realmHelper;
  private final DDPClientWraper ddpClient;
  private final AbstractRocketChatCacheObserver cacheObserver;
  private final Handler handler;
  private StreamRoomMessage streamRoomMessage;

  public StreamRoomMessageManager(Context context, String hostname,
                                  RealmHelper realmHelper, DDPClientWraper ddpClient) {
    this.context = context;
    this.hostname = hostname;
    this.realmHelper = realmHelper;
    this.ddpClient = ddpClient;

    cacheObserver = new AbstractRocketChatCacheObserver(context, realmHelper) {
      @Override
      protected void onRoomIdUpdated(String roomId) {
        unregisterStreamNotifyMessageIfNeeded();
        registerStreamNotifyMessage(roomId);
      }
    };
    handler = new Handler(Looper.myLooper());
  }

  private void registerStreamNotifyMessage(String roomId) {
    handler.post(() -> {
      streamRoomMessage = new StreamRoomMessage(context, hostname, realmHelper, ddpClient, roomId);
      streamRoomMessage.register();
    });
  }

  private void unregisterStreamNotifyMessageIfNeeded() {
    handler.post(() -> {
      if (streamRoomMessage != null) {
        streamRoomMessage.unregister();
        streamRoomMessage = null;
      }
    });
  }

  @Override
  public void register() {
    cacheObserver.register();
  }

  @Override
  public void unregister() {
    unregisterStreamNotifyMessageIfNeeded();
    cacheObserver.unregister();
  }
}
