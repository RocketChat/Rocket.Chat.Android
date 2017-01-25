package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class ImageKeyboardEditText extends EditText {

  private final String[] mimeTypes = {"image/gif"};

  final InputConnectionCompat.OnCommitContentListener inputConnectionListener =
      new InputConnectionCompat.OnCommitContentListener() {
        @Override
        public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                       Bundle opts) {
          if (listener != null) {
            return listener.onCommitContent(inputContentInfo, flags, opts, mimeTypes);
          }

          return false;
        }
      };

  private OnCommitContentListener listener;

  public ImageKeyboardEditText(Context context) {
    super(context);
  }

  public ImageKeyboardEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ImageKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ImageKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr,
                               int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
    final InputConnection inputConnection = super.onCreateInputConnection(editorInfo);

    EditorInfoCompat.setContentMimeTypes(editorInfo, mimeTypes);

    return InputConnectionCompat
        .createWrapper(inputConnection, editorInfo, inputConnectionListener);
  }

  public void setContentListener(OnCommitContentListener listener) {
    this.listener = listener;
  }

  public interface OnCommitContentListener {
    boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                            Bundle opts, String[] supportedMimeTypes);
  }
}
