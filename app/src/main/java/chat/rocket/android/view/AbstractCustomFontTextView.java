package chat.rocket.android.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

abstract class AbstractCustomFontTextView extends AppCompatTextView {

    protected abstract String getFont();

    //CHECKSTYLE:OFF RedundantModifier
    public AbstractCustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    //CHECKSTYLE:ON RedundantModifier

    //CHECKSTYLE:OFF RedundantModifier
    public AbstractCustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    //CHECKSTYLE:ON RedundantModifier

    //CHECKSTYLE:OFF RedundantModifier
    public AbstractCustomFontTextView(Context context) {
        super(context);
        init();
    }
    //CHECKSTYLE:ON RedundantModifier

    private void init() {
        String font = getFont();
        if (font != null) {
            Typeface typeface = TypefaceHelper.getTypeface(getContext(), font);
            if (typeface != null) setTypeface(typeface);
        }
    }
}
