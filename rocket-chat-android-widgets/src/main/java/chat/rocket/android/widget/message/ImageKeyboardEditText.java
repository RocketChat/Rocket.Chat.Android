package chat.rocket.android.widget.message;

import android.content.Context;
import android.os.Bundle;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class ImageKeyboardEditText extends AppCompatEditText {

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

  @Override
  public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
    final InputConnection inputConnection = super.onCreateInputConnection(editorInfo);
    if (inputConnection == null) {
      return null;
    }

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
