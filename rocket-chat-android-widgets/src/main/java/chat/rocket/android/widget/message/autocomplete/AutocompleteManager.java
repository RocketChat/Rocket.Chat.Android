package chat.rocket.android.widget.message.autocomplete;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.util.AppendOnlyLinkedArrayList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import chat.rocket.android.widget.R;

public class AutocompleteManager {

  private final Map<String, AutocompleteSource> autocompleteSourceMap = new HashMap<>();
  private AutocompleteSource currentSource;

  private Disposable afterTextChangeDisposable;
  private CompositeDisposable sourceDisposable = new CompositeDisposable();

  private EditText editText;
  private String text;
  private int fromIndex;
  private int toIndex;

  private final View contentHolder;
  private final RecyclerView recyclerView;

  private float contentHolderInitialY;
  private final float yOffset;

  private final AutocompleteSource.OnAutocompleteSelected onAutocompleteSelected =
      new AutocompleteSource.OnAutocompleteSelected() {
        @Override
        public void onSelected(String autocompleteSuggestion) {
          replaceSelected(autocompleteSuggestion);
        }
      };

  public AutocompleteManager(ViewGroup parent) {
    contentHolder =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.autocomplete_box, parent, false);
    contentHolder.setVisibility(View.GONE);

    recyclerView = (RecyclerView) contentHolder.findViewById(R.id.autocomplete_list);

    recyclerView.setLayoutManager(new LinearLayoutManager(parent.getContext()));

    parent.addView(contentHolder);

    yOffset = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        32,
        contentHolder.getContext().getResources().getDisplayMetrics()
    );
  }

  public void registerSource(AutocompleteSource autocompleteSource) {
    autocompleteSourceMap.put(autocompleteSource.getTrigger(), autocompleteSource);
  }

  public void bindTo(EditText editText, View anchor) {
    this.editText = editText;

    if (contentHolder.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
      RelativeLayout.LayoutParams layoutParams =
          (RelativeLayout.LayoutParams) contentHolder.getLayoutParams();
      layoutParams.addRule(RelativeLayout.ABOVE, anchor.getId());

      contentHolder.getViewTreeObserver().addOnGlobalLayoutListener(
          new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              contentHolderInitialY = contentHolder.getY();
              animateHide();
              contentHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
          });
    }

    afterTextChangeDisposable = RxTextView.afterTextChangeEvents(editText)
        .debounce(300, TimeUnit.MILLISECONDS)
        .filter(new AppendOnlyLinkedArrayList.NonThrowingPredicate<TextViewAfterTextChangeEvent>() {
          @Override
          public boolean test(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {
            return textViewAfterTextChangeEvent.editable() != null;
          }
        })
        .map(new Function<TextViewAfterTextChangeEvent, String>() {
          @Override
          public String apply(@NonNull TextViewAfterTextChangeEvent textViewAfterTextChangeEvent)
              throws Exception {
            //noinspection ConstantConditions
            return textViewAfterTextChangeEvent.editable().toString();
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(new Consumer<String>() {
          @Override
          public void accept(@NonNull String s) throws Exception {
            // let's do stateful things
            tryToAutocomplete(s);
          }
        })
        .subscribe();
  }

  public void dispose() {
    if (afterTextChangeDisposable != null) {
      afterTextChangeDisposable.dispose();
      afterTextChangeDisposable = null;
    }

    editText = null;

    cleanState();
  }

  private void tryToAutocomplete(String text) {
    if (editText == null) {
      cleanState();
      return;
    }

    final int selectionStart = editText.getSelectionStart();
    final int selectionEnd = editText.getSelectionEnd();
    if (selectionStart != selectionEnd) {
      // selecting text
      cleanState();
      return;
    }

    final String toCompleteText = getToCompleteText(text, selectionStart);

    final AutocompleteSource source = getSource(toCompleteText);
    if (source == null) {
      cleanState();
      return;
    }

    // render and stuff
    if (source != currentSource) {
      cleanState();
      currentSource = source;

      // set adapter on something
      recyclerView.setAdapter(currentSource.getAdapter());
      currentSource.setOnAutocompleteSelected(onAutocompleteSelected);
    }

    this.text = text;

    animateShow();

    sourceDisposable.clear();

    sourceDisposable.add(currentSource.loadList(toCompleteText));
  }

  private void cleanState() {
    animateHide();

    sourceDisposable.clear();

    text = null;

    if (currentSource != null) {
      currentSource.dispose();
      currentSource = null;
    }
  }

  private String getToCompleteText(String text, int cursorPosition) {
    if (text == null || text.length() == 0 || cursorPosition < 0
        || cursorPosition > text.length()) {
      return "";
    }

    final String[] textParts = text.split(" ");

    int currentPos = 0;
    for (String textPart : textParts) {
      int currentLength = currentPos + textPart.length();

      if (cursorPosition >= currentPos && cursorPosition <= currentLength) {
        fromIndex = currentPos;
        toIndex = cursorPosition;
        return textPart.substring(0, cursorPosition - currentPos);
      }

      currentPos = currentLength + 1;
    }

    return "";
  }

  private AutocompleteSource getSource(String toCompleteText) {
    if (toCompleteText == null || toCompleteText.length() == 0) {
      return null;
    }

    final String trigger = toCompleteText.substring(0, 1);

    return autocompleteSourceMap.get(trigger);
  }

  private void replaceSelected(String autocompleteSuggestion) {
    if (text == null) {
      return;
    }
    final String preText = text.substring(0, fromIndex);
    final String postText = text.substring(toIndex);

    StringBuilder stringBuilder =
        new StringBuilder(text.length() + autocompleteSuggestion.length());
    stringBuilder.append(preText)
        .append(autocompleteSuggestion)
        .append(' ');

    final int selectionPos = stringBuilder.length();

    stringBuilder.append(postText);

    editText.setText(stringBuilder.toString());
    editText.setSelection(selectionPos);
  }

  private void animateHide() {
    contentHolder.animate().cancel();
    contentHolder.animate()
        .alpha(0)
        .translationY(contentHolderInitialY + yOffset)
        .setDuration(150)
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            contentHolder.setVisibility(View.GONE);
          }
        });
  }

  private void animateShow() {
    contentHolder.animate().cancel();
    contentHolder.animate()
        .alpha(1)
        .translationY(contentHolderInitialY)
        .setDuration(150)
        .withStartAction(new Runnable() {
          @Override
          public void run() {
            contentHolder.setVisibility(View.VISIBLE);
          }
        });
  }
}
