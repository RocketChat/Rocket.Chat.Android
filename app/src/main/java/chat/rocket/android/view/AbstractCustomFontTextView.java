package chat.rocket.android.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

abstract class AbstractCustomFontTextView extends AppCompatTextView {

    protected abstract String getFont();

    public AbstractCustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AbstractCustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AbstractCustomFontTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        String font = getFont();
        if (font!=null) {
            Typeface tf = TypefaceHelper.getTypeface(getContext(), font);
            if (tf!=null) setTypeface(tf);
        }
    }
}
