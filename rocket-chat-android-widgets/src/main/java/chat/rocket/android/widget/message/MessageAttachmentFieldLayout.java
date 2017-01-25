package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emojione.Emojione;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.helper.InlineHightlighter;
import chat.rocket.android.widget.helper.Linkify;
import chat.rocket.android.widget.helper.MarkDown;

public class MessageAttachmentFieldLayout extends LinearLayout {

  private TextView titleView;
  private TextView valueView;

  public MessageAttachmentFieldLayout(Context context) {
    super(context);
    initialize(context, null);
  }

  public MessageAttachmentFieldLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public MessageAttachmentFieldLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MessageAttachmentFieldLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    setOrientation(VERTICAL);
    LayoutInflater.from(context)
        .inflate(R.layout.message_inline_attachment_field, this, true);

    titleView = (TextView) findViewById(R.id.field_title);
    valueView = (TextView) findViewById(R.id.field_value);
  }

  public void setTitle(String title) {
    titleView.setText(title);
  }

  public void setValue(String value) {
    valueView.setText(Emojione.shortnameToUnicode(value, false));

    MarkDown.apply(valueView);
    Linkify.markup(valueView);
    InlineHightlighter.highlight(valueView);
  }
}
