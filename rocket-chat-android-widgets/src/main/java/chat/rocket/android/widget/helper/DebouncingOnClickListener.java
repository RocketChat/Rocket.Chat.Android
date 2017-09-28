package chat.rocket.android.widget.helper;

import android.view.View;

/**
 * A {@linkplain View.OnClickListener click listener} that debounces multiple clicks posted in the
 * same frame. A click on one button disables all buttons for that frame.
 *
 * Shamelessly copied from butterknife -> https://github.com/JakeWharton/butterknife/blob/master/butterknife/src/main/java/butterknife/internal/DebouncingOnClickListener.java
 */
public abstract class DebouncingOnClickListener implements View.OnClickListener {
    static boolean enabled = true;

    private static final Runnable ENABLE_AGAIN = new Runnable() {
        @Override public void run() {
            enabled = true;
        }
    };

    @Override public final void onClick(View v) {
        if (enabled) {
            enabled = false;
            v.post(ENABLE_AGAIN);
            doClick(v);
        }
    }

    public abstract void doClick(View v);
}

