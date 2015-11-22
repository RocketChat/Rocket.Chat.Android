package chat.rocket.android;

import android.support.annotation.IdRes;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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

    public static void setClickable(TextView textView, final View.OnClickListener listener) {
        SpannableString text = new SpannableString(textView.getText());
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                listener.onClick(widget);
            }
        }, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(text);
    }
}
