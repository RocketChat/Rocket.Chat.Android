package chat.rocket.android.service.internal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.android.service.DDPClientRef;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.stream.StreamRoomMessage;

/**
 * wrapper for managing stream-notify-message depending on RocketChatCache.
 */
public class StreamRoomMessageManager implements Registrable {
  private final Context context;
  private final String hostname;
  private final RealmHelper realmHelper;
  private final DDPClientRef ddpClientRef;
  private final AbstractRocketChatCacheObserver cacheObserver;
  private final Handler handler;
  private StreamRoomMessage streamRoomMessage;

  public StreamRoomMessageManager(Context context, String hostname,
                                  RealmHelper realmHelper, DDPClientRef ddpClientRef) {
    this.context = context;
    this.hostname = hostname;
    this.realmHelper = realmHelper;
    this.ddpClientRef = ddpClientRef;

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
      streamRoomMessage = new StreamRoomMessage(context, hostname, realmHelper, ddpClientRef, roomId);
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
