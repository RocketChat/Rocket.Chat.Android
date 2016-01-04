package chat.rocket.android.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.widget.TextView;

import chat.rocket.android.R;
import hugo.weaving.DebugLog;

public class InlineHightlighter {
    //TODO: not completely implemented...
    // original implementation is RocketChat:packages/rocketchat-markdown/markdown.coffee
    // regex pattern with '/(^|&gt;|[ >_*~])\`([^`\r\n]+)\`([<_*~]|\B|\b|$)/gm'
    
    public static void highlight(TextView textview) {
        final CharSequence text = textview.getText();
        textview.setText(highlightInner(textview.getContext(), text));
    }

    private static CharacterStyle[] createCharStyles(final Context context) {
        Resources r = context.getResources();
        return new CharacterStyle[]{
                new ForegroundColorSpan(r.getColor(R.color.highlight_text_color)),
                new BackgroundColorSpan(r.getColor(R.color.highlight_text_background_color)),
                new StyleSpan(Typeface.BOLD),
                new TypefaceSpan("monospace")
        };
    }

    private static ForegroundColorSpan createTransparentSpan() {
        return new ForegroundColorSpan(Color.TRANSPARENT);
    }

    @DebugLog
    private static CharSequence highlightInner(final Context context, final CharSequence text) {
        final SpannableString s = new SpannableString(text);

        final int length = text.length();
        int highlightStart = length;
        for(int i=0; i<length; i++) {
            char c = text.charAt(i);
            if (c=='`'){
                if(i>highlightStart) {
                    final int highlightEnd = i;
                    if (highlightStart + 1 < highlightEnd) {
                        s.setSpan(createTransparentSpan(), highlightStart, highlightStart+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        for(CharacterStyle span : createCharStyles(context)) {
                            s.setSpan(span, highlightStart + 1, highlightEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        s.setSpan(createTransparentSpan(), highlightEnd, highlightEnd+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    highlightStart = length;
                }
                else {
                    highlightStart = i;
                }
            }
        }

        return s;
    }
}
