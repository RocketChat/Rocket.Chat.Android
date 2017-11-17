package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emojione.Emojione;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.helper.InlineHightlighter;
import chat.rocket.android.widget.helper.Linkify;
import chat.rocket.android.widget.helper.MarkDown;

/**
 */
public class RocketChatMessageLayout extends LinearLayout {
  private LayoutInflater inflater;

  public RocketChatMessageLayout(Context context) {
    super(context);
    initialize(context, null);
  }

  public RocketChatMessageLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public RocketChatMessageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RocketChatMessageLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                 int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    inflater = LayoutInflater.from(context);
    setOrientation(VERTICAL);
  }

  public void setText(String messageBody) {
    removeAllViews();
    if (messageBody.contains("```")) {
      boolean highlight = false;
      for (final String chunk : TextUtils.split(messageBody.replace("\r\n", "\n"), "```")) {
        final String trimmedChunk = chunk.replaceFirst("^\n", "").replaceFirst("\n$", "");
        if (highlight) {
          appendHighlightTextView(trimmedChunk);
        } else if (trimmedChunk.length() > 0) {
          appendTextView(trimmedChunk);
        }
        highlight = !highlight;
      }
    } else {
      appendTextView(messageBody);
    }
  }

  private void appendHighlightTextView(String text) {
    TextView textView = (TextView) inflater.inflate(R.layout.message_body_highlight, this, false);
    textView.setText(text);

    Linkify.markup(textView);

    addView(textView);
  }

  private void appendTextView(String text) {
    if (TextUtils.isEmpty(text)) {
      return;
    }

    TextView textView = (TextView) inflater.inflate(R.layout.message_body, this, false);
    textView.setText(Emojione.shortnameToUnicode(text, false));

    MarkDown.apply(textView);
    Linkify.markup(textView);
    InlineHightlighter.highlight(textView);

    addView(textView);
  }
}
