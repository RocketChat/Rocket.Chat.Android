package chat.rocket.android.util.extensions

import android.widget.EditText
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

fun EditText.asObservable(debounceTimeout: Long = 100): Observable<CharSequence> {
    return RxTextView.textChanges(this)
        .debounce(debounceTimeout, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(AndroidSchedulers.mainThread())
}