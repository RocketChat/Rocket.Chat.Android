package chat.rocket.android.widget.helper;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

public class LinkMovementMethodCompat extends LinkMovementMethod {
  private static LinkMovementMethodCompat sInstance;

  public static MovementMethod getInstance() {
    if (sInstance == null) {
      sInstance = new LinkMovementMethodCompat();
    }

    return sInstance;
  }

  @Override
  public boolean canSelectArbitrarily() {
    return true;
  }

  // http://stackoverflow.com/a/30572151/2104686
  @Override
  public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
    int action = event.getAction();

    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
      int eventX = (int) event.getX();
      int eventY = (int) event.getY();

      eventX -= widget.getTotalPaddingLeft();
      eventY -= widget.getTotalPaddingTop();

      eventX += widget.getScrollX();
      eventY += widget.getScrollY();

      Layout layout = widget.getLayout();
      int line = layout.getLineForVertical(eventY);
      int off = layout.getOffsetForHorizontal(line, eventX);

      ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

      if (link.length != 0) {
        if (action == MotionEvent.ACTION_UP) {
          link[0].onClick(widget);
        } else {
          Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
        }
        return true;
      }
    }

    return Touch.onTouchEvent(widget, buffer, event);
  }
}
