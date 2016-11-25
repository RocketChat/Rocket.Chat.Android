package chat.rocket.android.realm_helper;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class RealmModelViewHolder<T> extends RecyclerView.ViewHolder {

  public RealmModelViewHolder(View itemView) {
    super(itemView);
  }

  public abstract void bind(T model);
}
