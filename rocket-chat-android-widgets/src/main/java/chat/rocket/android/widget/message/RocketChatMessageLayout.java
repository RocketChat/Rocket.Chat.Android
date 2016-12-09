package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.helper.InlineHightlighter;
import chat.rocket.android.widget.helper.Linkify;
import com.emojione.Emojione;

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
    appendTextView(messageBody);
  }

  private void appendTextView(String text) {
    TextView textView = (TextView) inflater.inflate(R.layout.message_body, this, false);
    textView.setText(Emojione.shortnameToUnicode(text, false));

    Linkify.markup(textView);
    InlineHightlighter.highlight(textView);

    addView(textView);
  }
}
