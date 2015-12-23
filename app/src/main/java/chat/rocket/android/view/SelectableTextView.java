package chat.rocket.android.view;

import android.content.Context;
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;
import android.widget.TextView;

//ref: http://qiita.com/YusukeIwaki/items/ddc43c36415c23fac35f
public class SelectableTextView extends TextView {

    public SelectableTextView(Context context) {
        super(context);
        init();
    }

    public SelectableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTextIsSelectable(true);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (selStart == -1 || selEnd == -1) {
            // @hack : https://code.google.com/p/android/issues/detail?id=137509
            CharSequence text = getText();
            if (text instanceof Spannable) {
                Selection.setSelection((Spannable) text, 0, 0);
            }
        } else {
            super.onSelectionChanged(selStart, selEnd);
        }
    }
}
