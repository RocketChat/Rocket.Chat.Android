package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 */
public class RocketChatMessageAttachmentsLayout extends LinearLayout {
  private LayoutInflater inflater;

  public RocketChatMessageAttachmentsLayout(Context context) {
    super(context);
    initialize(context, null);
  }

  public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    inflater = LayoutInflater.from(context);
    setOrientation(VERTICAL);
  }

  public void setAttachments(String attachmentsString) {
    removeAllViews();

    try {
      JSONArray attachments = new JSONArray(attachmentsString);
      for (int i = 0; i < attachments.length(); i++) {
        JSONObject attachment = attachments.getJSONObject(i);
        appendAttachmentView(attachment);
      }
    } catch (JSONException exception) {
      return;
    }
  }

  private void appendAttachmentView(JSONObject attachmentObj) throws JSONException {

  }
}
