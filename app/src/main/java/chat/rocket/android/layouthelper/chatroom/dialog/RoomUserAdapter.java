package chat.rocket.android.layouthelper.chatroom.dialog;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.android.renderer.UserRenderer;

/**
 * RecyclerView adapter for UsersOfRooms.
 */
public class RoomUserAdapter extends RecyclerView.Adapter<RoomUserViewHolder> {

  private final Context context;
  private final LayoutInflater inflater;
  private final RealmHelper realmHelper;
  private final String hostname;
  private List<String> usernames;

  /**
   * Constructor with required parameters.
   */
  public RoomUserAdapter(Context context, RealmHelper realmHelper, String hostname) {
    this.context = context;
    this.inflater = LayoutInflater.from(context);
    this.realmHelper = realmHelper;
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

    RealmUser user = realmHelper.executeTransactionForRead(realm ->
        realm.where(RealmUser.class).equalTo(RealmUser.USERNAME, username).findFirst());
    if (user == null) {
      user = new RealmUser();
      user.setUsername(username);
      new UserRenderer(context, user.asUser())
          .avatarInto(holder.avatar, hostname)
          .usernameInto(holder.username);
    } else {
      new UserRenderer(context, user.asUser())
          .statusColorInto(holder.status)
          .avatarInto(holder.avatar, hostname)
          .usernameInto(holder.username);
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
