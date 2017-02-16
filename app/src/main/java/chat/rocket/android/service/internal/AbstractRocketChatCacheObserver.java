package chat.rocket.android.service.internal;

import android.content.Context;
import android.content.SharedPreferences;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.android.service.Registrable;

public abstract class AbstractRocketChatCacheObserver implements Registrable {
  private final Context context;
  private final RealmHelper realmHelper;
  private String roomId;
  private SharedPreferences.OnSharedPreferenceChangeListener listener =
      (prefs, key) -> {
        if (RocketChatCache.KEY_SELECTED_ROOM_ID.equals(key)) {
          updateRoomIdWith(prefs);
        }
      };

  protected AbstractRocketChatCacheObserver(Context context, RealmHelper realmHelper) {
    this.context = context;
    this.realmHelper = realmHelper;
  }

  private void updateRoomIdWith(SharedPreferences prefs) {
    String roomId = prefs.getString(RocketChatCache.KEY_SELECTED_ROOM_ID, null);
    if (!TextUtils.isEmpty(roomId)) {
      RealmRoom room = realmHelper.executeTransactionForRead(realm ->
          realm.where(RealmRoom.class).equalTo("rid", roomId).findFirst());
      if (room != null) {
        if (this.roomId == null || !this.roomId.equals(roomId)) {
          this.roomId = roomId;
          onRoomIdUpdated(roomId);
        }
        return;
      }
    }

    if (this.roomId != null) {
      this.roomId = null;
      onRoomIdUpdated(null);
    }
  }

  protected abstract void onRoomIdUpdated(String roomId);

  @Override
  public final void register() {
    SharedPreferences prefs = RocketChatCache.get(context);
    prefs.registerOnSharedPreferenceChangeListener(listener);
    updateRoomIdWith(prefs);
  }

  @Override
  public final void unregister() {
    RocketChatCache.get(context).unregisterOnSharedPreferenceChangeListener(listener);
  }
}
