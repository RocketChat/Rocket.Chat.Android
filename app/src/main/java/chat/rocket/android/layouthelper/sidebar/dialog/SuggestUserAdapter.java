package chat.rocket.android.layouthelper.sidebar.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.util.Iterator;
import java.util.List;
import chat.rocket.android.R;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.RealmAutoCompleteAdapter;
import chat.rocket.android.renderer.UserRenderer;
import chat.rocket.android.widget.RocketChatAvatar;

/**
 * adapter to suggest user names.
 */
public class SuggestUserAdapter extends RealmAutoCompleteAdapter<RealmUser> {
  private final String hostname;

  public SuggestUserAdapter(Context context, String hostname) {
    super(context, R.layout.listitem_room_user, R.id.room_user_name);
    this.hostname = hostname;
  }

  @Override
  protected void onBindItemView(View itemView, RealmUser user) {
    new UserRenderer(itemView.getContext(), user.asUser())
        .statusColorInto((ImageView) itemView.findViewById(R.id.room_user_status))
        .avatarInto((RocketChatAvatar) itemView.findViewById(R.id.room_user_avatar), hostname);
  }

  @Override
  protected void filterList(List<RealmUser> users, String text) {
    Iterator<RealmUser> itUsers = users.iterator();
    final String prefix = text.toLowerCase();
    while (itUsers.hasNext()) {
      RealmUser user = itUsers.next();
      if (!user.getUsername().toLowerCase().startsWith(prefix)) {
        itUsers.remove();
      }
    }
  }

  @Override
  protected String getStringForSelectedItem(RealmUser user) {
    return user.getUsername();
  }
}
