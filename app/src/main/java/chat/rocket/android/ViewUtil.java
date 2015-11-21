package chat.rocket.android;

import android.support.annotation.IdRes;
import android.view.View;
import android.widget.TextView;

public class ViewUtil {
    public static CharSequence getText(View root, @IdRes int id) {
        View v = root.findViewById(id);
        if(v instanceof TextView) {
            return ((TextView) v).getText();
        }
        else return "";
    }
}
