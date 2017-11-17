package chat.rocket.android.layouthelper.chatroom.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.renderer.UserRenderer;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmUser;

/**
 * RecyclerView adapter for UsersOfRooms.
 */
public class RoomUserAdapter extends RecyclerView.Adapter<RoomUserViewHolder> {

  private final Context context;
  private final LayoutInflater inflater;
  private final RealmHelper realmHelper;
  private final AbsoluteUrl absoluteUrl;
  private final String hostname;
  private List<String> usernames;

  /**
   * Constructor with required parameters.
   */
  public RoomUserAdapter(Context context, RealmHelper realmHelper, AbsoluteUrl absoluteUrl, String hostname) {
    this.context = context;
    this.inflater = LayoutInflater.from(context);
    this.realmHelper = realmHelper;
    this.absoluteUrl = absoluteUrl;
    this.hostname = hostname;
  }

  @Override
  public RoomUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = inflater.inflate(R.layout.listitem_room_user, parent, false);
    return new RoomUserViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(RoomUserViewHolder holder, int position) {
    String username = usernames.get(position);
    if (TextUtils.isEmpty(username)) {
      return;
    }

    RealmUser realmUser = realmHelper.executeTransactionForRead(realm ->
        realm.where(RealmUser.class).equalTo(RealmUser.USERNAME, username).findFirst());
    if (realmUser == null) {
      User user = User.builder()
          .setId("some-local-is")
          .setUsername(username)
          .setUtcOffset(0)
          .build();

      UserRenderer userRenderer = new UserRenderer(user);
      userRenderer.showAvatar(holder.avatar, hostname);
      userRenderer.showUsername(holder.username);
    } else {
      UserRenderer userRenderer = new UserRenderer(realmUser.asUser());
      userRenderer.showAvatar(holder.avatar, hostname);
      userRenderer.showUsername(holder.username);
      userRenderer.showStatusColor(holder.status);
    }
  }

  @Override
  public int getItemCount() {
    return usernames != null ? usernames.size() : 0;
  }

  public void setUsernames(List<String> usernames) {
    this.usernames = usernames;
    notifyDataSetChanged();
  }
}
