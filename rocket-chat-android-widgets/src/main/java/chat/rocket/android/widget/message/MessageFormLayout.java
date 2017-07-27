package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import chat.rocket.android.widget.R;

public class MessageFormLayout extends LinearLayout {

  protected ViewGroup composer;

  private ImageButton attachButton;
  private ImageButton sendButton;

  private ExtraActionSelectionClickListener extraActionSelectionClickListener;
  private SubmitTextListener submitTextListener;
  private ImageKeyboardEditText.OnCommitContentListener listener;

  public MessageFormLayout(Context context) {
    super(context);
    init();
  }

  public MessageFormLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    composer = (ViewGroup) LayoutInflater.from(getContext())
        .inflate(R.layout.message_composer, this, false);

    attachButton = composer.findViewById(R.id.button_attach);

    attachButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        onExtraActionSelectionClick();
      }
    });

    sendButton = composer.findViewById(R.id.button_send);

    sendButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String messageText = getText();
        if (messageText.length() > 0 && submitTextListener != null) {
          submitTextListener.onSubmitText(messageText);
        }
      }
    });

    sendButton.setScaleX(0);
    sendButton.setScaleY(0);
    sendButton.setVisibility(GONE);

    ImageKeyboardEditText editText = composer.findViewById(R.id.editor);

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (TextUtils.getTrimmedLength(s) > 0) {
          animateHide(attachButton);
          animateShow(sendButton);
        } else {
          animateShow(attachButton);
          animateHide(sendButton);
        }
      }
    });

    editText.setContentListener(new ImageKeyboardEditText.OnCommitContentListener() {
      @Override
      public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                     Bundle opts, String[] supportedMimeTypes) {
        if (listener != null) {
          return listener.onCommitContent(inputContentInfo, flags, opts, supportedMimeTypes);
        }
        return false;
      }
    });

    addView(composer);
  }

  public EditText getEditText() {
    return (EditText) composer.findViewById(R.id.editor);
  }

  public void setExtraActionSelectionClickListener(
      ExtraActionSelectionClickListener extraActionSelectionClickListener) {
    this.extraActionSelectionClickListener = extraActionSelectionClickListener;
  }

  public void setSubmitTextListener(SubmitTextListener submitTextListener) {
    this.submitTextListener = submitTextListener;
  }

  private void onExtraActionSelectionClick() {
    if (extraActionSelectionClickListener != null) {
      extraActionSelectionClickListener.onClick();
    }
  }

  private EditText getEditor() {
    return (EditText) composer.findViewById(R.id.editor);
  }

  public final String getText() {
    return getEditor().getText().toString().trim();
  }

  public final void setText(final CharSequence text) {
    final EditText editor = getEditor();
    editor.post(new Runnable() {
      @Override
      public void run() {
        editor.setText(text);
        if (text.length() > 0) {
          editor.setSelection(text.length());

          InputMethodManager inputMethodManager = (InputMethodManager) editor.getContext()
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          editor.requestFocus();
          inputMethodManager.showSoftInput(editor, 0);
        }
      }
    });
  }

  public void setEnabled(boolean enabled) {
    getEditor().setEnabled(enabled);
    composer.findViewById(R.id.button_send).setEnabled(enabled);
  }

  public void setEditTextCommitContentListener(
      ImageKeyboardEditText.OnCommitContentListener listener) {
    this.listener = listener;
  }

  private void animateHide(final View view) {
    view.animate().scaleX(0).scaleY(0).setDuration(150).withEndAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(GONE);
      }
    });
  }

  private void animateShow(final View view) {
    view.animate().scaleX(1).scaleY(1).setDuration(150).withStartAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(VISIBLE);
      }
    });
  }

  public interface ExtraActionSelectionClickListener {
    void onClick();
  }

  public interface SubmitTextListener {
    void onSubmitText(String message);
  }
}
