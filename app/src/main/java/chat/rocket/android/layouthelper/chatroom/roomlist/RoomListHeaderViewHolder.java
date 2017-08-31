package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import chat.rocket.android.R;

public class RoomListHeaderViewHolder extends RecyclerView.ViewHolder {
  private TextView title;
  private Button button;


  public RoomListHeaderViewHolder(View itemView) {
    super(itemView);
    title = itemView.findViewById(R.id.title);
    button = itemView.findViewById(R.id.btn_add);
  }

  public void bind(RoomListHeader roomListHeader) {
    final RoomListHeader.ClickListener clickListener = roomListHeader.getClickListener();
    if (clickListener == null) {
      button.setVisibility(View.GONE);
    } else {
      button.setVisibility(View.VISIBLE);
      button.setOnClickListener(view -> clickListener.onClick());
    }

    title.setText(roomListHeader.getTitle());
  }
}