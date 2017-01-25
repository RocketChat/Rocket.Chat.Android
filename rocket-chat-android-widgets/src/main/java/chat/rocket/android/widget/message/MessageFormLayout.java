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
import android.widget.LinearLayout;
import android.widget.TextView;

import chat.rocket.android.widget.R;

public class MessageFormLayout extends LinearLayout {

  protected ViewGroup composer;

  private View btnExtra;
  private View btnSubmit;

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

    btnExtra = composer.findViewById(R.id.btn_extras);

    btnExtra.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        onExtraActionSelectionClick();
      }
    });

    btnSubmit = composer.findViewById(R.id.btn_submit);

    btnSubmit.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String messageText = getText();
        if (messageText.length() > 0 && submitTextListener != null) {
          submitTextListener.onSubmitText(messageText);
        }
      }
    });

    btnSubmit.setScaleX(0);
    btnSubmit.setScaleY(0);
    btnSubmit.setVisibility(GONE);

    ImageKeyboardEditText editText = (ImageKeyboardEditText) composer.findViewById(R.id.editor);

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
          animateHide(btnExtra);
          animateShow(btnSubmit);
        } else {
          animateShow(btnExtra);
          animateHide(btnSubmit);
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

  private TextView getEditor() {
    return (TextView) composer.findViewById(R.id.editor);
  }

  public final String getText() {
    return getEditor().getText().toString().trim();
  }

  public final void setText(final CharSequence text) {
    final TextView editor = getEditor();
    editor.post(new Runnable() {
      @Override
      public void run() {
        editor.setText(text);
      }
    });
  }

  public void setEnabled(boolean enabled) {
    getEditor().setEnabled(enabled);
    composer.findViewById(R.id.btn_submit).setEnabled(enabled);
  }

  public void setEditTextContentListener(ImageKeyboardEditText.OnCommitContentListener listener) {
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
