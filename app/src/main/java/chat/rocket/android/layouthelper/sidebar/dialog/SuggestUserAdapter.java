package chat.rocket.android.layouthelper.sidebar.dialog;

import android.content.Context;
import android.view.View;

import java.util.Iterator;
import java.util.List;
import chat.rocket.android.R;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.RealmAutoCompleteAdapter;
import chat.rocket.android.renderer.UserRenderer;

/**
 * adapter to suggest user names.
 */
public class SuggestUserAdapter extends RealmAutoCompleteAdapter<RealmUser> {
  private final AbsoluteUrl absoluteUrl;

  public SuggestUserAdapter(Context context, AbsoluteUrl absoluteUrl) {
    super(context, R.layout.listitem_room_user, R.id.room_user_name);
    this.absoluteUrl = absoluteUrl;
  }

  @Override
  protected void onBindItemView(View itemView, RealmUser user) {
    new UserRenderer(itemView.getContext(), user.asUser())
        .statusColorInto(itemView.findViewById(R.id.room_user_status))
        .avatarInto(itemView.findViewById(R.id.room_user_avatar), absoluteUrl);
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
