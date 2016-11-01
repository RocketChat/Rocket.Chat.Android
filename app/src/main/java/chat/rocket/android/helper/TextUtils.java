package chat.rocket.android.helper;

public class TextUtils {
    public static boolean isEmpty(CharSequence str) {
        // same definition as android.text.TextUtils#isEmpty().
        return str == null || str.length() == 0;
    }

    public static CharSequence or(CharSequence str, CharSequence defaultValue) {
        if (isEmpty(str)) return defaultValue;
        return str;
    }
}
