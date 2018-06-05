package chat.rocket.android.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import kotlin.coroutines.experimental.CoroutineContext


class TransformedLiveData<Source, Output>(
    private val runContext: CoroutineContext = CommonPool,
    private val source: LiveData<Source>,
    private val transformation: (Source?) -> Output?)
    : LiveData<Output>() {
    private var job: Job? = null

    private val observer = Observer<Source> { source ->
        job?.cancel()
        job = launch(runContext) {
            transformation(source)?.let { transformed ->
                // Could have used postValue instead, but using the UI context I can guarantee that
                // a canceled job will never emit values.
                withContext(UI) {
                    value = transformed
                }
            }
        }
    }

    override fun onActive() {
        source.observeForever(observer)
    }

    override fun onInactive() {
        job?.cancel()
        source.removeObserver(observer)
    }
}

fun <Source, Output> LiveData<Source>.transform(
    runContext: CoroutineContext = CommonPool,
    transformation: (Source?) -> Output?) = TransformedLiveData(runContext, this, transformation)