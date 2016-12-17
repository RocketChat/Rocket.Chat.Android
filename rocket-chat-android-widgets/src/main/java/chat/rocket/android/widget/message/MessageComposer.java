package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import chat.rocket.android.widget.R;

public class MessageComposer extends LinearLayout {

  protected ActionListener actionListener;
  protected ViewGroup composer;

  public MessageComposer(Context context) {
    super(context);
    init();
  }

  public MessageComposer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public MessageComposer(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MessageComposer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  public final void setOnActionListener(@Nullable ActionListener listener) {
    actionListener = listener;
  }

  private void init() {
    composer = (ViewGroup) LayoutInflater.from(getContext())
        .inflate(R.layout.message_composer, this, false);
    composer.findViewById(R.id.btn_submit).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String messageText = getText();
        if (messageText.length() > 0) {
          if (actionListener != null) {
            actionListener.onSubmit(messageText);
          }
        }
      }
    });

    addView(composer);
  }

  private TextView getEditor() {
    return (TextView) composer.findViewById(R.id.editor);
  }

  public final String getText() {
    return getEditor().getText().toString().trim();
  }

  public final void setText(CharSequence text) {
    getEditor().setText(text);
  }

  public void setEnabled(boolean enabled) {
    getEditor().setEnabled(enabled);
    composer.findViewById(R.id.btn_submit).setEnabled(enabled);
  }

  protected final void focusToEditor() {
    final TextView editor = getEditor();
    editor.requestFocus();
    InputMethodManager imm =
        (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT);
  }

  protected final void unFocusEditor() {
    InputMethodManager imm =
        (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
  }

  public void show(@Nullable Runnable callback) {
    focusToEditor();
    setVisibility(View.VISIBLE);
    if (callback != null) {
      callback.run();
    }
  }

  public void hide(@Nullable Runnable callback) {
    unFocusEditor();
    setVisibility(View.GONE);
    if (callback != null) {
      callback.run();
    }
  }

  public boolean isShown() {
    return getVisibility() == View.VISIBLE;
  }

  public interface ActionListener {
    void onSubmit(String message);

    void onCancel();
  }
}
