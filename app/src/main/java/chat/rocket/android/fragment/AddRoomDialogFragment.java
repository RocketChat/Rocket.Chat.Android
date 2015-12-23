package chat.rocket.android.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.SyncState;

public class AddRoomDialogFragment extends DialogFragment {

    private Room.Type mRoomType;

    public static AddRoomDialogFragment create(Room.Type type) {
        Bundle args = new Bundle();
        args.putString("type",type.getValue());

        AddRoomDialogFragment f = new AddRoomDialogFragment();
        f.setArguments(args);
        return f;
    }

    public AddRoomDialogFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args==null || !args.containsKey("type")) throw new IllegalArgumentException();

        mRoomType = Room.Type.getType(args.getString("type"));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = new AlertDialog.Builder(getContext(), R.style.AppDialog)
                .setTitle("Add Room")
                .setView(R.layout.dialog_add_room)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextView txtRoomName = (TextView) getDialog().findViewById(R.id.add_room_name);

                        if (TextUtils.isEmpty(txtRoomName.getText())) return;

                        Room r = new Room();
                        r.id = "DUMMY";
                        r.type = mRoomType;
                        r.name = txtRoomName.getText().toString();
                        r.syncstate = SyncState.NOT_SYNCED;
                        r.putByContentProvider(getContext());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();

        if (mRoomType == Room.Type.DIRECT_MESSAGE) {
            d.setTitle("Direct Message with...");
        }

        return d;
    }
}
