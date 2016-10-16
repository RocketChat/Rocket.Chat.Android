package chat.rocket.android.content.observer;

import android.content.Context;
import android.net.Uri;
import android.os.Looper;

import chat.rocket.android.api.ws.RocketChatWSAPI;

public class RoomsHandler extends AbstractObserver {

    public RoomsHandler(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    public Uri getTargetUri() {
        return null;
    }

    @Override
    protected void onChange(Uri uri) {

    }
}
