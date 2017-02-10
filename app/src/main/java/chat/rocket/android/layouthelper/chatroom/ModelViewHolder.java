package chat.rocket.android.layouthelper.chatroom;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class ModelViewHolder<T> extends RecyclerView.ViewHolder {

  public ModelViewHolder(View itemView) {
    super(itemView);
  }

  public abstract void bind(T model);
}
