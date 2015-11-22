package chat.rocket.android.view;

import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;

public class LinkMovementMethodCompat extends LinkMovementMethod {
    @Override
    public boolean canSelectArbitrarily() {
        return true;
    }

    public static MovementMethod getInstance() {
        if (sInstance == null) sInstance = new LinkMovementMethodCompat();

        return sInstance;
    }

    private static LinkMovementMethodCompat sInstance;
}
