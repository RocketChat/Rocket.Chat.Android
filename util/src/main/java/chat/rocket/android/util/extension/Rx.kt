package chat.rocket.android.util.extension

import android.widget.EditText
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun EditText.asObservable(): Observable<CharSequence> {
    return RxTextView.textChanges(this)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}