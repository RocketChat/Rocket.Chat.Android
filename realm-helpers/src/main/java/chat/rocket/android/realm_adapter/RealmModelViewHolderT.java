package chat.rocket.android.realm_adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import io.realm.RealmObject;

public abstract class RealmModelViewHolder<T extends RealmObject> extends RecyclerView.ViewHolder {

  public RealmModelViewHolder(View itemView) {
    super(itemView);
  }

  public abstract void bind(T model);
}
